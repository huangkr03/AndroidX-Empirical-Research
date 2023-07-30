package com.res.soot.traverse;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.LinkedList;
import java.util.List;


class MySootClass {

    @JSONField(name="name", ordinal=1)
    public String name;

    @JSONField(name="call_fields", ordinal=2)
    public List<String> fields = new LinkedList<>();

    @JSONField(name="call_methods", ordinal=3)
    public List<String> methods = new LinkedList<>();
}
