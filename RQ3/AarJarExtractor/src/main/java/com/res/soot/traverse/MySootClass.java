package com.res.soot.traverse;

import java.util.ArrayList;
import java.util.List;

class MySootClass {
    @JSONField(name = "className", ordinal = 1)
    public String name;
    @JSONField(name = "fields", ordinal = 1)
    public List<String> fields = new ArrayList<>();
    @JSONField(name = "methods", ordinal = 2)
    public List<String> methods = new ArrayList<>();
}
