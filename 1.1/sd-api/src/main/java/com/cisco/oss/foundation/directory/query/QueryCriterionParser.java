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
import java.util.List;

import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery.ContainQueryCriterion;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery.EqualQueryCriterion;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery.InQueryCriterion;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery.NotContainQueryCriterion;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery.NotEqualQueryCriterion;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery.NotInQueryCriterion;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery.PatternQueryCriterion;

/**
 * It is a Parser to convert QueryCriterion from/to commandline expression string
 *
 *
 */
public class QueryCriterionParser {

    /**
     * convert the QueryCriterion to a String expression.
     *
     * @param queryCriterion
     *         the QueryCriterion.
     * @return
     *         the String expression.
     */
    private static String criterionToExpressionStr(QueryCriterion queryCriterion){
        if(queryCriterion instanceof EqualQueryCriterion){
            EqualQueryCriterion criterion = (EqualQueryCriterion) queryCriterion;
            return criterion.getMetadataKey() + " equals " + escapeString(criterion.getCriterion());
        } else if(queryCriterion instanceof NotEqualQueryCriterion){
            NotEqualQueryCriterion criterion = (NotEqualQueryCriterion) queryCriterion;
            return criterion.getMetadataKey() + " not equals " + escapeString(criterion.getCriterion());
        } else if(queryCriterion instanceof ContainQueryCriterion){
            ContainQueryCriterion criterion = (ContainQueryCriterion) queryCriterion;
            return criterion.getMetadataKey();
        } else if(queryCriterion instanceof NotContainQueryCriterion){
            NotContainQueryCriterion criterion = (NotContainQueryCriterion) queryCriterion;
            return "not " + criterion.getMetadataKey();
        } else if(queryCriterion instanceof InQueryCriterion){
            InQueryCriterion criterion = (InQueryCriterion) queryCriterion;
            StringBuilder sb = new StringBuilder(criterion.getMetadataKey());
            sb.append(" in [ ");
            for(String s : criterion.getCriterion()){
                sb.append(escapeString(s)).append(", ");
            }
            sb.append("]");
            return sb.toString();
        } else if(queryCriterion instanceof NotInQueryCriterion){
            NotInQueryCriterion criterion = (NotInQueryCriterion) queryCriterion;
            StringBuilder sb = new StringBuilder(criterion.getMetadataKey());
            sb.append(" not in [ ");
            for(String s : criterion.getCriterion()){
                sb.append(escapeString(s)).append(", ");
            }
            sb.append("]");
            return sb.toString();
        } else if(queryCriterion instanceof PatternQueryCriterion){
            PatternQueryCriterion criterion = (PatternQueryCriterion) queryCriterion;
            return criterion.getMetadataKey() + " matches " + escapeString(criterion.getCriterion());
        }
        return "";
    }

    /**
     * Parse from statement String expression to QueryCriterion.
     *
     * @param statement
     *         the statement String expression.
     * @return
     *         the QueryCriterion.
     */
    public static QueryCriterion toQueryCriterion(String statement){
        char [] delimiter = {' ', '\t', ',', '[', ']'};
        List<String> cmdList = splitStringByDelimiters(statement, delimiter, true);
        List<String> tokenList = filterDelimiterElement(cmdList, delimiter);
        if(tokenList.size() == 1){
            String key = cmdList.get(0);
            if(! key.isEmpty()){
                return new ContainQueryCriterion(key);
            }
        } else if(tokenList.size() == 2){
            String cmd = cmdList.get(0);
            String key = cmdList.get(1);
            if(! key.isEmpty() && cmd.equals("not")){
                return new NotContainQueryCriterion(key);
            }
        } else {
            String key = tokenList.get(0);
            String op = tokenList.get(1);
            if(op.equals("not")){
                op = op + " " + tokenList.get(2);
            }

            if(op.equals("equals")){
                String value = unescapeString(tokenList.get(2));
                if(value != null){
                    return new EqualQueryCriterion(key, value);
                }
            } else if(op.equals("not equals")){
                String value = unescapeString(tokenList.get(3));
                if(value != null){
                    return new NotEqualQueryCriterion(key, value);
                }
            } else if(op.equals("matches")){
                String value = unescapeString(tokenList.get(2));
                if(value != null){
                    return new PatternQueryCriterion(key, value);
                }
            } else if(op.equals("in")){
                List<String> values = new ArrayList<String>();
                boolean start = false;
                for(String s : cmdList){
                    if(s.isEmpty() || s.equals(",")){
                        continue;
                    } else if(s.equals("[")){
                        start = true;
                        continue;
                    } else if(s.equals("]")){
                        start = false;
                        break;
                    } else if(start){
                        String v = unescapeString(s);
                        if(v != null){
                            values.add(v);
                        }
                    }
                }
                return new InQueryCriterion(key, values);
            } else if(op.equals("not in")){
                List<String> values = new ArrayList<String>();
                boolean start = false;
                for(String s : cmdList){
                    if(s.isEmpty() || s.equals(",")){
                        break;
                    } else if(s.equals("[")){
                        start = true;
                        continue;
                    } else if(s.equals("]")){
                        start = false;
                        break;
                    } else if(start){
                        String v = unescapeString(s);
                        if(v != null){
                            values.add(v);
                        }

                    }
                }
                return new NotInQueryCriterion(key, values);
            }
        }
        return null;
    }

    /**
     * Parse the ServiceInstanceQuery from command line String expression.
     *
     * Deserialize the ServiceInstanceQuery command line to the ServiceInstanceQuery.
     *
     * @param cli
     *         the ServiceInstanceQuery command line String expression.
     * @return
     *         the QueryCriterion statement String list.
     */
    public static ServiceInstanceQuery toServiceInstanceQuery(String cli){
        char [] delimiters = {';'};
        ServiceInstanceQuery query = new ServiceInstanceQuery();
        for(String statement : splitStringByDelimiters(cli, delimiters, false)){
            QueryCriterion criterion = toQueryCriterion(statement);
            if(criterion != null){
                query.addQueryCriterion(criterion);
            }
        };
        return query;
    }

    /**
     * Convert the ServiceInstanceQuery to the command line string expression.
     *
     * @param query
     *         the ServiceInstanceQuery.
     * @return
     *         the string expression.
     */
    public static String queryToExpressionStr(ServiceInstanceQuery query){
        List<QueryCriterion> criteria = query.getCriteria();
        StringBuilder sb = new StringBuilder();
        for(QueryCriterion criterion : criteria){
            String statement = criterionToExpressionStr(criterion);
            if(statement != null && ! statement.isEmpty()){
                sb.append(statement).append(";");
            }
        }
        return sb.toString();
    }

    /**
     * Escape the String for the QueryCriterion String.
     *
     * Add " to the head and tail, switch " to \".
     *
     * @param str
     *         the origin String.
     * @return
     *         the escaped String
     */
    private static String escapeString(String str){
        return "\"" +  str.replaceAll("\"", "\\\\\"") + "\"";
    }

    /**
     * Unescape the QueryCriterion String.
     *
     * remove the head and tail ", switch \" to ".
     *
     * @param str
     *         the escaped String.
     * @return
     *         the origin String.
     */
    private static String unescapeString(String str){
        if(str.startsWith("\"") && str.endsWith("\"")){
            String s = str.replaceAll("\\\\\"", "\"");
            return s.substring(1, s.length() -1);
        }
        return null;
    }

    /**
     * Filter the delimiter from the String array.
     *
     * remove the String element in the delimiter.
     *
     * @param arr
     *         the target array.
     * @param delimiters
     *         the delimiter array need to remove.
     * @return
     *         the array List.
     */
    private static List<String> filterDelimiterElement(List<String> arr, char[] delimiters){
        List<String> list = new ArrayList<String>();
        for(String s : arr){
            if(s == null || s.isEmpty()){
                continue;
            }
            if(s.length() > 1){
                list.add(s);
                continue;
            }
            char strChar = s.charAt(0);
            boolean find = false;
            for(char c : delimiters){
                if(c == strChar){
                    find = true;
                    break;
                }
            }
            if(find == false){
                list.add(s);
            }
        }
        return list;
    }

    /**
     * Split a complete String to String array by the delimeter array.
     *
     * This method used to split a command or a statement to sub elements.
     * It will trim the sub elements too.
     *
     * @param str
     *         the complete string.
     * @param delimiters
     *         the delimiter array.
     * @param includeDeli
     *         if true, include the delimiter in the return array.
     * @return
     *         the split String array.
     */
    private static List<String> splitStringByDelimiters(String str, char[] delimiters, boolean includeDeli){
        List<String> arr = new ArrayList<String>();
        int i = 0, start = 0, quota = 0;
        char pre = 0;
        for(char c : str.toCharArray()){
            if(c == '"' && pre != '\\'){
                quota ++;
            }
            if(quota % 2 == 0){
                for(char deli : delimiters){
                    if( c == deli){
                        if(includeDeli){
                            arr.add(str.substring(start, i).trim());
                            start = i;
                        }else if(i > start ){
                            arr.add(str.substring(start, i).trim());
                            start = i + 1;
                        }

                    }
                }

            }
            i ++;
            pre = c;
        }

        if(includeDeli){
            arr.add(str.substring(start, i).trim());
            start = i;
        }else if(i > start ){
            arr.add(str.substring(start, i).trim());
        }

        return arr;
    }
}
