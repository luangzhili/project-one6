package cn.itcast.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Set;

/**
 * DATE:2018/12/5
 * USER:lzlWhite
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/applicationContext-redis.xml")
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;
    //测试字符串objing
    @Test
    public void testString(){
        redisTemplate.boundValueOps("string_key").set("船只博客");
        Object obj = redisTemplate.boundValueOps("string_key").get();
        System.out.println(obj);
    }
    // 测试散列 hash
    @Test
    public void testHash() {
        redisTemplate.boundHashOps("hash_key").put("f1", "v1");
        redisTemplate.boundHashOps("hash_key").put("f2", "v2");
        redisTemplate.boundHashOps("hash_key").put("f3", "v3");
        List list = redisTemplate.boundHashOps("hash_key").values();
        System.out.println(list);
    }
    // 测试列表 list
    @Test
    public void testList() {
        redisTemplate.boundListOps("list_key").leftPush(1);
        redisTemplate.boundListOps("list_key").leftPush(4);
        redisTemplate.boundListOps("list_key").rightPush(2);
        redisTemplate.boundListOps("list_key").rightPush(3);
        List list_key = redisTemplate.boundListOps("list_key").range(0, -1);
        System.out.println(list_key);
    }
    // 测试集合 set
    @Test
    public void testSet() {
        redisTemplate.boundSetOps("set_key").add(1, 3, 5, "itcast", 7);
        Set set = redisTemplate.boundSetOps("set_key").members();
        System.out.println(set);
    }
    // 测试有序集合 sorted set
    @Test
    public void testSortedSet() {
        redisTemplate.boundZSetOps("zset_key").add("aa", 20);
        redisTemplate.boundZSetOps("zset_key").add("bb", 10);
        Set set = redisTemplate.boundZSetOps("zset_key").range(0, -1);
        System.out.println(set);
    }
}
