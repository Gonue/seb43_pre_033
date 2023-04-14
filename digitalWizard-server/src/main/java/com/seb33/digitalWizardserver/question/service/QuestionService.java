package com.seb33.digitalWizardserver.question.service;

import com.seb33.digitalWizardserver.exception.BusinessLogicException;
import com.seb33.digitalWizardserver.exception.ExceptionCode;
import com.seb33.digitalWizardserver.member.entity.Member;
import com.seb33.digitalWizardserver.member.repository.MemberRepository;
import com.seb33.digitalWizardserver.question.dto.QuestionDto;
import com.seb33.digitalWizardserver.question.entity.Question;
import com.seb33.digitalWizardserver.question.repository.QuestionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final MemberRepository memberRepository;

    public QuestionService(QuestionRepository questionRepository, MemberRepository memberRepository) {
        this.questionRepository = questionRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public void create(String title, String body, String email){
        Member member = memberRepository.findByEmail(email).orElseThrow(()->
                new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND, String.format("%s 을 찾을 수 없습니다.", email)));
        questionRepository.save(Question.of(title, body, member));
    }

    @Transactional
    public QuestionDto update(String title, String body, String email, Long questionId){
        Member member = memberRepository.findByEmail(email).orElseThrow(()->
                new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND, String.format("%s 을 찾을 수 없습니다", email)));
        Question question = questionRepository.findById(questionId).orElseThrow(() ->
                new BusinessLogicException(ExceptionCode.QUESTION_NOT_FOUND, String.format("%s 을 찾을 수 없습니다.",questionId)));
        if(question.getMember() != member){
            throw new BusinessLogicException(ExceptionCode.INVALID_PERMISSION, String.format("%s 작성 유저가 권한을 가지고 있지 않습니다.", email));
        }
        question.setTitle(title);
        question.setBody(body);
        return QuestionDto.from(questionRepository.save(question));
    }

    public void delete(String email, Long questionId){
        Member member = memberRepository.findByEmail(email).orElseThrow(()->
                new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND, String.format("%s 을 찾을 수 없습니다.", email)));
        Question question = questionRepository.findById(questionId).orElseThrow(()->
                new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND, String.format("%s 을 찾을 수 없습니다.",questionId)));
        if (!Objects.equals(question.getMember().getMemberId(), member.getMemberId())){
            throw new BusinessLogicException(ExceptionCode.INVALID_PERMISSION, String.format("%s 는 %s 의 권한이 없습니다.", email,questionId));
        }
        questionRepository.delete(question);
    }

    public Page<QuestionDto> list(Pageable pageable){
        return questionRepository.findAll(pageable).map(QuestionDto::from);
    }

    public Page<QuestionDto> myQuestionList(String email, Pageable pageable){
        Member member = memberRepository.findByEmail(email).orElseThrow(() ->
                new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND, String.format("%s 을 찾을 수 없습니다.", email)));
        return questionRepository.findAllByMember(member, pageable).map(QuestionDto::from);
    }
}
