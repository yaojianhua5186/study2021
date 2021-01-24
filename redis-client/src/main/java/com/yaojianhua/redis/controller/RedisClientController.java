package com.yaojianhua.redis.controller;

import com.yaojianhua.redis.util.JedisPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import static java.lang.Thread.*;

@RestController
@Slf4j
public class RedisClientController {

    private static final String redisUrl = "10.253.104.253";
    private static final int serverPort_M = 6379;   //Master
    private static final int serverPort_S = 6380;   //Slaveof

    //测试连接Redis数据库的连通性
    @GetMapping(value="/client/redis/connected")
    public String getConnected(){
        Jedis jedis = new Jedis(redisUrl,serverPort_M);
        String result = jedis.ping();
        return "测试redis连通性 ： "+result;
    }

   //测试主从复制---一般在Redis服务端进行配置
    @GetMapping("/client/redis/getMS")
    public String  getRedisMS() throws InterruptedException {
        Jedis jedis_M = new Jedis(redisUrl,serverPort_M);   //master
        Jedis jedis_S = new Jedis(redisUrl,serverPort_S);   //slaveof

        //从机连接主机
        jedis_S.slaveof(redisUrl,serverPort_M);

        //主机执行写操作；从机支持读操作
        String result_M = jedis_M.set("k1","111222");

        String  reslut_S = jedis_S.get("k1");
        log.info("result_M = "+result_M+"   reslut_S = "+reslut_S);
        return "reslut_S = "+reslut_S;
    }

    /**
     * 通俗点讲，watch命令就是标记一个键，如果标记了一个键，
     * 在提交事务前如果该键被别人修改过，那事务就会失败，这种情况通常可以在程序中重新再尝试一次。
     * 首先标记了键balance,然后检查余额是否足够，不足就取消标记，并不做扣减；
     * 足够的话，就启动事务进行更新操作。
     * 如果在此期间键balance被其他人修改，那在提交事务（执行exec）时就会报错，
     * 程序中通常可以捕获这类错误异常重新执行一次，知道成功。
     */

    //测试Redis事务
    @GetMapping("/client/redis/getTX")
    public boolean transMethod() throws InterruptedException {
        Jedis jedis = new Jedis(redisUrl,serverPort_M);
        int balance;    //总额
        int debt;       //欠额
        int amtToSubtract = 10; //实际消费金额

        jedis.watch("balance"); //监控一个key
        log.info("启动事务MULTI");
        log.info("暂停7秒");
        Thread.sleep(10000);
        balance = Integer.parseInt(jedis.get("balance")); //获取key=balance的值,并转换为int
        if(balance < amtToSubtract){//总额不够消费,取消监控,并返回刷卡失败
            jedis.unwatch();
            log.info("modify  balance = "+balance+"  amtToSubtract = "+amtToSubtract);
            return false;
        }else{
            Transaction transaction = jedis.multi();    //开始事务
            transaction.decrBy("balance",amtToSubtract);
            transaction.incrBy("debt",amtToSubtract);
            transaction.exec();  //执行事务。

            balance = Integer.parseInt(jedis.get("balance"));
            debt = Integer.parseInt(jedis.get("debt"));
            log.info("success  balance = "+balance+"  amtToSubtract = "+amtToSubtract);
            return true;
        }
    }


    //测试Jedis pool
    @GetMapping(value="/client/redis/pool")
    public String testPool(){
        //获取连接池
        JedisPool jedisPool= JedisPoolUtil.getJedisPoolInstance();
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            jedis.set("class","class111");
            String result = jedis.get("class");
            log.info("success class = "+result);
            return result;
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            //前置关闭连接
            JedisPoolUtil.release(jedisPool,jedis);
        }
        return null;
    }

}
