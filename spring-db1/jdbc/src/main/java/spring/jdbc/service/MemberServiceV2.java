package spring.jdbc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.jdbc.domain.Member;
import spring.jdbc.repository.MemberRepositoryV1;
import spring.jdbc.repository.MemberRepositoryV2;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 파라미터 연동, 풀을 고려한 종료
 */
@RequiredArgsConstructor
@Slf4j
public class MemberServiceV2 {

    private final DataSource dataSource;  //의존관계 주입 필요
    private final MemberRepositoryV2 memberRepository;

    /**
     * fromId의 회원을 조회해서 toId 회원에게 money만큼의 돈을 계좌이체 하는 로직 (커넥션을 파라미터로 전달하여 같은 커넥션이 사용되도록 유지)
     */
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Connection con = dataSource.getConnection();

        try {
            con.setAutoCommit(false);  //트랜잭션 시작

            //비지니스 로직
            bizLogic(fromId, toId, money, con);
            con.commit();  //로직 정상 수행시 커밋
        } catch (Exception e) {
            con.rollback();  //실패시 롤백
            throw new IllegalStateException(e);
        } finally {
            release(con);
        }

    }

    private void bizLogic(String fromId, String toId, int money, Connection con) throws SQLException {
        Member formMember = memberRepository.findById(con, fromId);
        Member toMember = memberRepository.findById(con, toId);

        memberRepository.update(con, fromId, formMember.getMoney() - money);  //트랜잭션 없으면 기본적으로 자동 커밋
        validation(toMember);
        memberRepository.update(con, toId, formMember.getMoney() + money);
    }

    private void release(Connection con) {
        if (con != null) {
            try {
                con.setAutoCommit(true); //커넥션 풀 고려(기본이 auto 커밋)
                con.close();
            } catch (Exception e) {
                log.info("error", e);
            }
        }
    }

    private void validation(Member toMember) {
        //예외 상황을 테스트해보기 위해 toId가 "ex"인 경우 예외를 발생
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }

}
