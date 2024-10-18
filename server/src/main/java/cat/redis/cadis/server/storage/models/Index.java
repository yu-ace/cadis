package cat.redis.cadis.server.storage.models;

import java.io.Serializable;

public class Index implements Serializable {
    private Integer position;
    private Integer length;
    private String type;
    private Integer pageNumber;
    public Index() {
    }

    public Index(Integer position, Integer length, String type, Integer pageNumber) {
        this.position = position;
        this.length = length;
        this.type = type;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }
}
