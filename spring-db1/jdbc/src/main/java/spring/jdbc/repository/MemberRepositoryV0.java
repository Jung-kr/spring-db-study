package spring.jdbc.repository;

import lombok.extern.slf4j.Slf4j;
import spring.jdbc.connection.DBConnectionUtil;
import spring.jdbc.domain.Member;

import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JDBC - DriverManager 사용
 */
@Slf4j
public class MemberRepositoryV0 {

    /**
     * 1)데이터 저장
     * @param member
     * @return
     * @throws SQLException
     */
    public Member save(Member member) throws SQLException {
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
            log.error("db error", e);
            throw e;
        } finally {
            //사용한 con, psmt 리소스 정리(역순으로)
            close(con, pstmt, null);
        }
    }

    /**
     * 2)데이터 조회
     * @param memberId
     * @return
     * @throws SQLException
     */
    public Member findById(String memberId) throws SQLException {
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
            log.error("db error", e);
            throw e;
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
    public void update(String memberId, int money) throws SQLException {
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
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    /**
     * 4)데이터 삭제
     * @param memberId
     * @throws SQLException
     */
    public void delete(String memberId) throws SQLException{
        String sql = "delete from member where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, memberId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    private void close(Connection con, Statement stmt, ResultSet rs) {

        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
    }

    private Connection getConnection() throws SQLException {
        return DBConnectionUtil.getConnection();
    }
}
