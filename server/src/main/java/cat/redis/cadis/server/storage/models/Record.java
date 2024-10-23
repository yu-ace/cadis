package cat.redis.cadis.server.storage.models;

public class Record {
    private String key;
    private byte[] value;
    private Integer type;
    private Integer list; // 0 true 1 false
    public Record() {
    }

    public Record(String key, byte[] value, Integer type,Integer list) {
        this.key = key;
        this.value = value;
        this.type = type;
        this.list = list;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getList() {
        return list;
    }

    public void setList(Integer list) {
        this.list = list;
    }
}
