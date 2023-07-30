package com.res.soot.traverse;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;
import java.util.List;

class MyAPK {
    @JSONField(name = "classes", ordinal = 1)
    public List<MySootClass> compassMethods = new ArrayList<>();
}
