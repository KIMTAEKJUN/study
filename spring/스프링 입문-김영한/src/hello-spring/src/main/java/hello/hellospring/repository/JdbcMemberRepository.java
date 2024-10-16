package hello.hellospring.repository;

import hello.hellospring.domain.Member;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcMemberRepository implements MemberRepository {
    private final DataSource dataSource;

    public JdbcMemberRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Member save(Member member) {
        // 쿼리문
        String sql = "insert into member(name) values(?)";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // conn으로 연결을 실행하고
            conn = getConnection();

            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            // parameterIndex = 1은 위의 쿼리문에 있는 values(?)와 매칭된다.
            pstmt.setString(1, member.getName());
            // 실제 쿼리가 DB로 직접 날아간다.
            pstmt.executeUpdate();

            // 자동으로 키를 반환해준다.
            rs = pstmt.getGeneratedKeys();

            // rs가 가져온 값은 처음에는 데이터를 읽을 수 없는 가장 앞쪽에 있어서, 그다음 데이터에 값이 있으면 값을 꺼낸다.
            if (rs.next()) {
                member.setId(rs.getLong(1));
            } else { // 데이터가 없을 시
                throw new SQLException("id 조회 실패");
            }
            return member;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            // 자원들을 릴리즈 해줘야 한다. -> db connection 같은 경우는 외부 네트워크가 연결 된 거기 때문에 바로바로 전원을 꺼줘야 한다.
            // 그러지 않으면 db connection 이 계속 쌓이다가 대 장애가 날 수도 있다.
            close(conn, pstmt, rs);
        }
    }
    @Override
    public Optional<Member> findById(Long id) {
        // 쿼리문
        String sql = "select * from member where id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, id);

            // 조회는 executeUpdate가 아니라 executeQuery이다.
            // 실제 쿼리가 DB로 직접 날아간다.
            rs = pstmt.executeQuery();

            // rs가 가져온 값은 처음에는 데이터를 읽을 수 없는 가장 앞쪽에 있어서, 그다음 데이터에 값이 있으면
            if(rs.next()) {
                // 멤버 객체를 만들고, 반환을 해준다.
                Member member = new Member();
                member.setId(rs.getLong("id"));
                member.setName(rs.getString("name"));
                return Optional.of(member);
            } else { // 데이터가 없을 시 비어있는 Optional 객체를 가져온다.
                return Optional.empty();
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            close(conn, pstmt, rs);
        }
    }
    @Override
    public List<Member> findAll() {
        // 쿼리문
        String sql = "select * from member";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();

            pstmt = conn.prepareStatement(sql);

            rs = pstmt.executeQuery();

            // 리스트 컬렉션에 담기
            List<Member> members = new ArrayList<>();

            // 쭉 루프를 돌리며, 리스트 컬렉션에 멤버를 담은 후 반환한다.
            while(rs.next()) {
                Member member = new Member();
                member.setId(rs.getLong("id"));
                member.setName(rs.getString("name"));
                members.add(member);
            }
            return members;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            close(conn, pstmt, rs);
        }
    }
    @Override
    public Optional<Member> findByName(String name) {
        // 쿼리문
        String sql = "select * from member where name = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);

            rs = pstmt.executeQuery();
            if(rs.next()) {
                Member member = new Member();
                member.setId(rs.getLong("id"));
                member.setName(rs.getString("name"));
                return Optional.of(member);
            }
            return Optional.empty();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            close(conn, pstmt, rs);
        }
    }
    private Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }
    private void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (pstmt != null) {
                pstmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (conn != null) {
                close(conn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void close(Connection conn) throws SQLException {
        DataSourceUtils.releaseConnection(conn, dataSource);
    }
}
