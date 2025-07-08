package org.choon.careerbee.domain.interview.repository.custom;

import java.util.List;
import org.choon.careerbee.domain.interview.dto.response.InterviewProblemResp.InterviewProblemInfo;

public interface InterviewProblemCustomRepository {

    List<InterviewProblemInfo> fetchFirstInterviewProblemsByType();
}
