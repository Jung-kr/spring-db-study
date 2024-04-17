package spring.jdbc.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import spring.jdbc.domain.Member;
import spring.jdbc.repository.ex.MyDbException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * SQLExceptionTranslator 추가
 */
@Slf4j
public class MemberRepositoryV4_2 implements MemberRepository {

    private final DataSource dataSource;
    private final SQLExceptionTranslator extranslator;

    public MemberRepositoryV4_2(DataSource dataSource) {

        this.dataSource = dataSource;
        this.extranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
    }

    /**
     * 1)데이터 저장
     * @param member
     * @return
     * @throws SQLException
     */
    @Override
    public Member save(Member member) {
        String sql = "insert into member(member_id, money) values(?, ?)";

        Connection con = null;  //연결
        PreparedStatement pstmt = null;  //SQL 전달(Statement 의 자식 타입인데, ? 를 통한 파라미터 바인딩을 가능하게 해줌)

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            //파라미터 바인딩
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());

            pstmt.executeUpdate();
            return member;
        } catch (SQLException e) {
            throw extranslator.translate("save", sql, e);
        } finally {
            //사용한 con, psmt 리소스 정리(역순으로)
            close(con, pstmt, null);
        }
    }

    /**
     * 2)데이터 조회 - 트랜잭션 X
     * @param memberId
     * @return
     * @throws SQLException
     */
    @Override
    public Member findById(String memberId) {
        String sql = "select * from member where member_id = ?";

        Connection con = null;  //연결
        PreparedStatement pstmt = null;  //SQL을 담은 내용
        ResultSet rs = null;  //SQL 요청 응답

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
                throw new NoSuchElementException("member not found memberId=" + memberId);
            }
        } catch (SQLException e) {
            throw extranslator.translate("findById", sql, e);
        } finally {
            close(con, pstmt, rs);
        }
    }

    /**
     * 3)데이터 수정
     * @param memberId
     * @param money
     * @throws SQLException
     */
    @Override
    public void update(String memberId, int money) {
        String sql = "update member set money=? where member_id=?";

        Connection con = null;  //연결
        PreparedStatement pstmt = null;  //SQL을 담은 내용

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);

            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            throw extranslator.translate("update", sql, e);
        } finally {
            close(con, pstmt, null);
        }
    }

    /**
     * 4)데이터 삭제
     * @param memberId
     * @throws SQLException
     */
    @Override
    public void delete(String memberId) {
        String sql = "delete from member where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, memberId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw extranslator.translate("delete", sql, e);
        } finally {
            close(con, pstmt, null);
        }
    }

    private void close(Connection con, Statement pstmt, ResultSet rs) {
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(pstmt);
        //주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다.
        DataSourceUtils.releaseConnection(con, dataSource);
    }

    private Connection getConnection() throws SQLException {
        //주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다.
        Connection con = DataSourceUtils.getConnection(dataSource);
        log.info("get connection={}, class={}", con, con.getClass());
        return con;
    }
}
