package com.jercel.tech.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JercelUrlValidator {
    public static boolean validateURL(String url, String regex){
        log.info("Inside validateURl, url : {} validation_regex : {}",url,regex);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        return matcher.find();
    }
}
