package hokkori.web.service;

import java.io.InputStream;
import java.util.List;

import jakarta.transaction.Transactional;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hokkori.web.dto.AllianceMemberDto;
import hokkori.web.dto.ChatMessageDto;
import hokkori.web.entity.AllianceMember;
import hokkori.web.model.discord.DIscordEventListener;
import hokkori.web.model.member.MemberModel;
import hokkori.web.repository.AllianceMemberRepository;
import jp.highwide.common.excel.ExcelUtil;

@Service
public class MemberService implements DIscordEventListener {

	Logger log = LoggerFactory.getLogger( MemberService.class );

	@Autowired
	private AllianceMemberRepository allianceMemberRepository;

	@Autowired
	private MemberModel memberModel;

	@Transactional
	public void removeAllianceMemberDto( Integer id ) {
		allianceMemberRepository.deleteById( id );
		log.info( "メンバー削除:" + id );
	}

	public AllianceMemberDto getAllianceMemberDtoByAyarabuName( String ayarabuName ) {
		return memberModel.getAllianceMemberDtoByAyarabuName( ayarabuName );
	}

	public List<AllianceMemberDto> getAllianceMemberDtoList() {
		return memberModel.getAllianceMemberDtoList();
	}

	public void addAllianceMemberDto( AllianceMemberDto AllianceMemberDto ) {
		memberModel.addAllianceMemberDto( AllianceMemberDto );
	}

	@Transactional
	public void updateAllianceMemberDto( AllianceMemberDto AllianceMemberDto ) {
		memberModel.updateAllianceMemberDto( AllianceMemberDto );
	}

	@Transactional
	public void removeAllianceMemberDtoByDiscordId( AllianceMemberDto allianceMemberDto ) {
		AllianceMember allianceMember = allianceMemberRepository.findByDiscordMemberId( allianceMemberDto.getDiscordMemberId() );

		allianceMemberRepository.deleteById( allianceMember.getId() );
		log.info( "メンバー削除:" + allianceMemberDto );
	}

	/**
	 * メンバーの一覧をエクセルに出力する。
	 * 
	 * @return メンバー一覧のエクセル
	 */
	public XSSFWorkbook downloadExcel() {
		XSSFWorkbook excel = ExcelUtil.writeBeanToExcel( allianceMemberRepository.findAll() , AllianceMember.class );
		return excel;
	}

	/**
	 * メンバーに書かれた戦力を登録する
	 * 
	 * @param is エクセルファイルのインプットストリーム
	 */
	public void uploadExcel( InputStream is ) {
		try {
			List<AllianceMember> allianceMemberList = ExcelUtil.readExcelToBean( 0 , 1 , is , AllianceMember.class );
			allianceMemberList.forEach( allianceMember -> {
				allianceMemberRepository.save( allianceMember );
			} );
		}
		catch( Exception e ) {
			log.error( "エクセルアップロードエラー" , e );
		}
	}

	@Override
	public void onGuildMemberJoin( AllianceMemberDto allianceMemberDto ) {
		if( allianceMemberDto == null )
			return;
		memberModel.addOrChangeAllianceMemberDto( allianceMemberDto );
	}

	@Override
	public void onGuildMemberRemove( AllianceMemberDto allianceMemberDto ) {
		// TODO discord抜けたら脱退扱いでいいか？
		removeAllianceMemberDtoByDiscordId( allianceMemberDto );
	}

	@Override
	public void onMessageReceived( ChatMessageDto chatMessageDto ) {
	}

	@Override
	public void onMessageUpdate( ChatMessageDto chatMessageDto ) {
	}

	@Override
	public void onMessageDelete( String messageId ) {
	}

}
