package leetcode.clone.leetcode.response;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class TestCaseSubmission {

    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "testcases_id")
    private TestCases testCases;

    private Boolean isSuccess;
    private Boolean isTimedOut;
    private Boolean isMemoryExceeded;

    @ManyToOne
    @JoinColumn(name = "submission_id")
    private Submission submission;
}
