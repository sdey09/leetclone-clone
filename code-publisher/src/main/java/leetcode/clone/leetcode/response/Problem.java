package leetcode.clone.leetcode.response;

import jakarta.persistence.*;
import leetcode.clone.leetcode.convertor.StringToListConverter;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Problem {

    @Id
    private Long id;
    private String fn;
    private String basicCode;
    private String header;
//    private String metadata;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "problem_id")
    private List<TestCases> testCases = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "problem_id")
    private List<CodeSnippets> codeSnippets = new ArrayList<>();
}
