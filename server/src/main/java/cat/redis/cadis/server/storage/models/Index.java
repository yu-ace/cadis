package cat.redis.cadis.server.storage.models;

import java.io.Serializable;

public class Index implements Serializable {
    private Integer position;
    private Integer length;
    private Integer type;
    private Integer list; // 0 true 1 false
    private Integer pageNumber;
    public Index() {
    }

    public Index(Integer position, Integer length, Integer type, Integer list, Integer pageNumber) {
        this.position = position;
        this.length = length;
        this.type = type;
        this.list = list;
        this.pageNumber = pageNumber;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
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

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }
}
