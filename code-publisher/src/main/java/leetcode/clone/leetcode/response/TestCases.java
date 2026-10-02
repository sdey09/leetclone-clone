package leetcode.clone.leetcode.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "testcases")
public class TestCases {

    @Id
    private Long id;
    private String explanation;
    private String inputParams;
    private String expectedOutput;

    @OneToOne
    @JsonIgnore
    private Problem problem;
}