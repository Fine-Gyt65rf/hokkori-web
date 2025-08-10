package hokkori.web.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import hokkori.web.entity.AllianceMember;

@Repository
public interface  AllianceMemberRepository extends JpaRepository<AllianceMember, Integer>{
	public AllianceMember findByAyarabuName(String ayarabuName);
	public AllianceMember findByDiscordName(String discordName);
	public AllianceMember findByDiscordMemberId(String discordMemberId);
	public List<AllianceMember> findAllByAlliance(String alliance);
}
