package cat.redis.cadis.client.model;

public class CommandResult {
    String name;
    String key;
    Integer type; // 0 int 1 string 2 object
    Boolean list;
    byte[]data; // value
    String functionName; // 指令名称
    Boolean result; //是否有结果
    public CommandResult() {
    }

    public CommandResult(String name, String key, Integer type, Boolean list,
                         byte[] data,String functionName, Boolean result) {
        this.name = name;
        this.key = key;
        this.type = type;
        this.list = list;
        this.data = data;
        this.functionName = functionName;
        this.result = result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Boolean getList() {
        return list;
    }

    public void setList(Boolean list) {
        this.list = list;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }
}
