/**
 * Copyright 2014 Cisco Systems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cisco.oss.foundation.directory.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery.ContainQueryCriterion;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery.EqualQueryCriterion;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery.InQueryCriterion;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery.NotContainQueryCriterion;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery.NotEqualQueryCriterion;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery.NotInQueryCriterion;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery.PatternQueryCriterion;

public class QueryTest {

    @Test
    public void test(){

        Map<String, String> meta1 = new HashMap<String, String>();
        meta1.put("solution", "core");
        meta1.put("datacenter", "data1");
        meta1.put("address", "usa");
        meta1.put("age", "1");


        Map<String, String> meta2 = new HashMap<String, String>();
        meta2.put("node", "node01");
        meta2.put("solution", "cim");
        meta2.put("datacenter", "data5");
        meta2.put("address", "sf");
        meta2.put("age", "10");

        NotEqualQueryCriterion query1 = new NotEqualQueryCriterion("solution", "core");
        Assert.assertTrue(query1.isMatch(meta2));
        Assert.assertFalse(query1.isMatch(meta1));

        EqualQueryCriterion query2 = new EqualQueryCriterion("solution", "cim");
        Assert.assertTrue(query2.isMatch(meta2));
        Assert.assertFalse(query2.isMatch(meta1));

        PatternQueryCriterion query3 = new PatternQueryCriterion("datacenter", "data[123]");
        Assert.assertTrue(query3.isMatch(meta1));
        Assert.assertFalse(query3.isMatch(meta2));

        ContainQueryCriterion query4 = new ContainQueryCriterion("node");
        Assert.assertTrue(query4.isMatch(meta2));
        Assert.assertFalse(query4.isMatch(meta1));

        NotContainQueryCriterion query5 = new NotContainQueryCriterion("node");
        Assert.assertTrue(query5.isMatch(meta1));
        Assert.assertFalse(query5.isMatch(meta2));

        ArrayList<String> list = new ArrayList<String>();
        list.add("usa");
        list.add("box");
        InQueryCriterion query6 = new InQueryCriterion("address", list);
        Assert.assertTrue(query6.isMatch(meta1));
        Assert.assertFalse(query6.isMatch(meta2));

        ArrayList<String> list2 = new ArrayList<String>();
        list2.add("1");
        list2.add("2");
        NotInQueryCriterion query7 = new NotInQueryCriterion("age", list2);
        Assert.assertTrue(query7.isMatch(meta2));
        Assert.assertFalse(query7.isMatch(meta1));
    }
}
