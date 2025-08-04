package com.FourAM.RepoScribe.Model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String githubUserName;
    private String repoLink;
    private boolean containsAPI;
}
