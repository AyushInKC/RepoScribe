package com.FourAM.RepoScribe.Model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data

@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String repoLink;
    private boolean containsAPI;

    public void setId(String id) {
        this.id = id;
    }

    public void setRepoLink(String repoLink) {
        this.repoLink = repoLink;
    }

    public void setContainsAPI(boolean containsAPI) {
        this.containsAPI = containsAPI;
    }

    public String getId() {
        return id;
    }
    public String getRepoLink() {
        return repoLink;
    }

    public boolean isContainsAPI() {
        return containsAPI;
    }
}
