package leetcode.clone.leetcode.response;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "code_snippets")
public class CodeSnippets {
    @Id
    private Long id;
    private String lang;
    private String langSlug;
    private String code;

    @OneToOne
    @JsonIgnore
    private Problem problem;
}
