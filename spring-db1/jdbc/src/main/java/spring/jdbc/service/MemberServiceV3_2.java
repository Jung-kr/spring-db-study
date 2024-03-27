package spring.jdbc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import spring.jdbc.domain.Member;
import spring.jdbc.repository.MemberRepositoryV3;

import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 템플릿
 */
@Slf4j
public class MemberServiceV3_2 {

    private final TransactionTemplate txTemplate;
    private final MemberRepositoryV3 memberRepository;

    public MemberServiceV3_2(PlatformTransactionManager transactionManager, MemberRepositoryV3 memberRepository) {
        this.txTemplate = new TransactionTemplate(transactionManager);
        this.memberRepository = memberRepository;
    }

    /**
     * fromId의 회원을 조회해서 toId 회원에게 money만큼의 돈을 계좌이체 하는 로직 (커넥션을 파라미터로 전달하여 같은 커넥션이 사용되도록 유지)
     */
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        //status : 기존의 TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        txTemplate.executeWithoutResult((status) -> {
            //비지니스 로직
            try {
                bizLogic(fromId, toId, money);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        Member formMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, formMember.getMoney() - money);  //트랜잭션 없으면 기본적으로 자동 커밋
        validation(toMember);
        memberRepository.update(toId, formMember.getMoney() + money);
    }

    private void validation(Member toMember) {
        //예외 상황을 테스트해보기 위해 toId가 "ex"인 경우 예외를 발생
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }

}
