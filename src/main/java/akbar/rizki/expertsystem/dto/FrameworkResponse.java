package akbar.rizki.expertsystem.dto;

import akbar.rizki.expertsystem.entity.Framework;

public class FrameworkResponse {

    public Integer id;
    public String name;
    public String type;
    public String language;
    public boolean isActive;

    public static FrameworkResponse from(Framework framework) {
        FrameworkResponse r = new FrameworkResponse();
        r.id = framework.id;
        r.name = framework.name;
        r.type = framework.type;
        r.language = framework.language;
        r.isActive = framework.isActive;
        return r;
    }
}
