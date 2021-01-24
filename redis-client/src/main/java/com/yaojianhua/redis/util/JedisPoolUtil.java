package com.yaojianhua.redis.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisPoolUtil {
    //定义个池子
    private static volatile JedisPool  jedisPool=  null;
    private static final String redisUrl = "10.253.104.253";
    private static final int serverPort = 6379;   //Master

    //构造私有化
    private JedisPoolUtil(){ }

    //返回一个JedisPool 实例
    public static JedisPool getJedisPoolInstance(){
        if(null == jedisPool){
            //加上同步块
            synchronized (JedisPoolUtil.class){
                if(null == jedisPool){
                    //配置
                    JedisPoolConfig poolConfig = new JedisPoolConfig();
                    poolConfig.setMaxTotal(1000);
                    poolConfig.setMaxIdle(32); //预留多少空闲
                    poolConfig.setMaxWaitMillis(100*1000); //最大等等
                    poolConfig.setTestOnBorrow(true);   //建立连通性，ping
                    jedisPool = new JedisPool(poolConfig,redisUrl,serverPort);
                    System.out.println(" jedis  = "+jedisPool.getResource().getClass());
                }
            }
        }

        return jedisPool;
    }

    //关闭连接
    public static void release(JedisPool jedisPool, Jedis jedis){
        if(null != jedis){
            Jedis jedis1 = null;
            try{
                jedis1 = jedisPool.getResource();
            }finally{
                jedis1.close();
            }
        }
    }
}
