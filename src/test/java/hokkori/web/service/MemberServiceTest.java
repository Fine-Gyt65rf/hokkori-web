package hokkori.web.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import hokkori.web.dto.AllianceMemberDto;
import hokkori.web.dto.MemberAlliance;
import hokkori.web.dto.MemberRole;

@Sql(scripts = "classpath:ddl.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:default.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SpringBootTest
class MemberServiceTest {
	@Autowired
	private MemberService memberService;

	@Test
	void testRemoveAllianceMemberDto() {
		/*
		AllianceMemberDto removeDto = memberModel.getAllianceMemberDto(1L);
		allianceMemberDtoList.remove(removeDto);
        // DTOリストから削除されたか確認
		assertEquals(0, memberModel.getAllianceMemberDtoList().size());
		allianceMemberRepository.deleteById(1L);
		*/
		
	}

	@Test
	void testInit() {
        String name = "Fine";
        String discordId = "fine4139";
        memberService.init(name, discordId, false);


        AllianceMemberDto allianceDto = memberService.getAllianceMemberDto(discordId);
        assertNotNull(allianceDto);
        assertEquals(name, allianceDto.getDiscordName());
        assertEquals(discordId, allianceDto.getDiscordMemberId());
        assertEquals(MemberRole.MEMBER, allianceDto.getMemberRole());
        assertEquals(MemberAlliance.HOKKORI, allianceDto.getAlliance());
	}

	@Test
	void testInit_withNoExistingMember() {
        String name = "Fine";
        String discordId = "fine4139";
        memberService.init(name, discordId, false);


        AllianceMemberDto allianceDto = memberService.getAllianceMemberDto(discordId);
        assertNotNull(allianceDto);
        assertEquals(name, allianceDto.getDiscordName());
        assertEquals(discordId, allianceDto.getDiscordMemberId());
        assertEquals("メンバー", allianceDto.getMemberRole());
        assertEquals("ほっこり茶屋", allianceDto.getAlliance());
	}
	
	@Test
	void testGetAllianceMemberDtoList() {
		fail("まだ実装されていません");
	}

	@Test
	void testAddOrChangeAllianceMemberDto() {
		fail("まだ実装されていません");
	}

	@Test
	void testRemoveAllianceMemberDtoByDiscordId() {
		fail("まだ実装されていません");
	}

	@Test
	void testGetAllianceMemberDtoString() {
		fail("まだ実装されていません");
	}

	@Test
	void testGetAllianceMemberDtoLong() {
		fail("まだ実装されていません");
	}
	
}
