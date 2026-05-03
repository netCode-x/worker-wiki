package com.codebit.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * Lua 脚本Script
 */
@Configuration
public class RedisScriptConfig {


    @Bean
    public RedisScript<Long> checkAndDeleteScript(){
           String script=
                   "local storedCode = redis.call('GET', KEYS[1]) " +
                           "if storedCode == false then " +
                           "    return -1 " +
                           "elseif storedCode == ARGV[1] then " +
                           "    redis.call('DEL', KEYS[1]) " +
                           "    return 1 " +
                           "else " +
                           "    return 0 " +
                           "end";

        return RedisScript.of(script,Long.class);
    }


}
