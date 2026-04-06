package org.tavall.webstore.admin.input;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageSectionAdminInput {

    private Long id;
    private Long contentPageId;
    private String sectionKey;
    private String displayName;
    private String sectionType;
    private String placement;
    private boolean active;
    private int position;
    private int mobilePosition;
    private String configurationJson;
}
