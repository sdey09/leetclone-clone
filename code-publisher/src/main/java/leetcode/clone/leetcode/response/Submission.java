package leetcode.clone.leetcode.response;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Submission {

    @Id
    private Long id;

    private Boolean is_correct;
    private Long time_taken;

    @ManyToOne
    @JoinColumn(name = "problem_id")
    private Problem problem;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "testcases_id")
    private List<TestCases> testcase;
}