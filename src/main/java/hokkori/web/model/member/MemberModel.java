package hokkori.web.model.member;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import hokkori.web.HokkoriWebApplication;
import hokkori.web.dto.AllianceMemberDto;
import hokkori.web.dto.MemberAlliance;
import hokkori.web.dto.MemberRole;
import hokkori.web.entity.AllianceMember;
import hokkori.web.entity.ChannelMaster;
import hokkori.web.entity.ChatMessageView;
import hokkori.web.repository.AllianceMemberRepository;
import hokkori.web.repository.ChannelMasterRepository;
import hokkori.web.repository.ChatMessageViewRepository;

@Component
public class MemberModel {

	Logger log = LoggerFactory.getLogger( MemberModel.class );

	@Autowired
	private AllianceMemberRepository allianceMemberRepository;

	@Autowired
	private ChatMessageViewRepository chatMessageViewRepository;

	@Autowired
	private ChannelMasterRepository channelMasterRepository;

	private ModelMapper modelMapper;

	public MemberModel() {
		modelMapper = new ModelMapper();
	}

	/**
	 * 指定されたユーザー情報（名前・ID・Botフラグ）に基づいて、
	 * ユーザー情報の初期化処理を行う。
	 * 
	 * DB上に該当ユーザー（discordId）が存在しない場合:
	 * デフォルトの {@link AllianceMemberDto} を新規作成し、DBおよびメモリ上のリストに追加する。
	 * このとき、isBotがtrueであれば管理者（LEADER）権限を自動付与する。
	 *
	 * DB上に既に存在する場合:
	 * DBの情報をDTOへ変換し、メモリ上のリストに反映（更新）する。
	 * この処理により、最新のDB情報が同期される。
	 *
	 * 本メソッドは、Bot起動時や初回参加時などに呼ばれ、
	 * ユーザーの存在確認および初期登録・更新を担う。
	 * 
	 * @param name Discord上の表示名
	 * @param discordId DiscordユーザーID
	 * @param isBot Botかどうかを示すフラグ（true の場合、リーダー権限を付与）
	 */
	@Async
	@Transactional
	public void init( String name , String discordId , boolean isBot ) {
		AllianceMemberDto allianceMemberDto = new AllianceMemberDto();
		AllianceMember allianceMember = allianceMemberRepository.findByDiscordMemberId( discordId );
		if( allianceMember == null ) {
			allianceMemberDto = getDefault( name , discordId , isBot );
			addOrChangeAllianceMemberDto( allianceMemberDto );
		}
		else {
			allianceMemberDto = toDtoFromEntity( allianceMember );
			addOrChangeAllianceMemberDto( allianceMemberDto );
		}
	}

	private AllianceMemberDto getDefault( String name , String discordId , boolean isBot ) {
		AllianceMemberDto allianceMemberDto = new AllianceMemberDto();
		if( isBot ) {
			allianceMemberDto.setMemberRole( MemberRole.LEADER );
			allianceMemberDto.setAyarabuId( "ayarabu" );
			allianceMemberDto.setAyarabuName( name );
		}
		else {
			allianceMemberDto.setMemberRole( MemberRole.MEMBER );
			allianceMemberDto.setAyarabuId( "" );
			allianceMemberDto.setAyarabuName( "" );
		}
		allianceMemberDto.setBot( false );
		allianceMemberDto.setCreateDate( HokkoriWebApplication.sdf.format( new Date() ) );
		allianceMemberDto.setStatementCount( 0 );
		allianceMemberDto.setAlliance( MemberAlliance.NONE );
		allianceMemberDto.setDiscordMemberId( discordId );
		allianceMemberDto.setDiscordName( name );
		allianceMemberDto.setBot( isBot );
		return allianceMemberDto;
	}

	@Transactional
	public synchronized void addOrChangeAllianceMemberDto( AllianceMemberDto allianceMemberDto ) {
		AllianceMember allianceMember;

		String ayarabuName = allianceMemberDto.getAyarabuName().trim();
		allianceMember = allianceMemberRepository.findByAyarabuName( ayarabuName );
		if( allianceMember == null || ayarabuName.isEmpty() ) {
			String discordMemberId = allianceMemberDto.getDiscordMemberId();
			if( discordMemberId == null || discordMemberId.equals( "" ) ) {
				allianceMember = null;
			}
			else {
				allianceMember = allianceMemberRepository.findByDiscordMemberId( allianceMemberDto.getDiscordMemberId() );
			}

			if( allianceMember == null ) {
				allianceMember = toEntityFromDto( allianceMemberDto );
				allianceMember = allianceMemberRepository.save( allianceMember );
				List<ChannelMaster> channelList = channelMasterRepository.findAll();
				for( ChannelMaster channelMaster : channelList ) {
					ChatMessageView chatMessageView = new ChatMessageView();
					chatMessageView.setChannelId( channelMaster.getChannelId() );
					chatMessageView.setChatMessageId( 0 );
					chatMessageView.setMemberId( allianceMember.getId() );
					chatMessageViewRepository.save( chatMessageView );
				}
				log.info( "メンバー追加:" + allianceMemberDto );
			}
			else {
				int id = allianceMember.getId();
				allianceMember = toEntityFromDto( allianceMemberDto );
				allianceMember.setId( id );
				allianceMember = allianceMemberRepository.save( allianceMember );
				log.info( "メンバー更新1:" + allianceMemberDto );
			}
		}
		else {
			int id = allianceMember.getId();
			allianceMember = toEntityFromDto( allianceMemberDto );
			allianceMember.setId( id );
			allianceMember = allianceMemberRepository.save( allianceMember );
			log.info( "メンバー更新2:" + allianceMemberDto );
		}
	}

	public void addAllianceMemberDto( AllianceMemberDto AllianceMemberDto ) {
		addOrChangeAllianceMemberDto( AllianceMemberDto );
	}

	@Transactional
	public void updateAllianceMemberDto( AllianceMemberDto AllianceMemberDto ) {
		addOrChangeAllianceMemberDto( AllianceMemberDto );
	}

	public List<AllianceMemberDto> getAllianceMemberDtoList() {
		List<AllianceMemberDto> allianceMemberDtoList = new ArrayList<AllianceMemberDto>();
		List<AllianceMember> allianceMemberList = allianceMemberRepository.findAll();
		allianceMemberList.forEach( allianceMember -> {
			allianceMemberDtoList.add( toDtoFromEntity( allianceMember ) );
		} );
		return allianceMemberDtoList;
	}

	public AllianceMember toEntityFromDto( AllianceMemberDto allianceMemberDto ) {
		ModelMapper modelMapper = new ModelMapper();
		// modelMapper.addConverter(new StringToMemberRoleConverter());
		return modelMapper.map( allianceMemberDto , AllianceMember.class );
	}

	public AllianceMemberDto toDtoFromEntity( AllianceMember allianceMember ) {
		if( allianceMember == null ) {
			return null;
		}
		ModelMapper modelMapper = new ModelMapper();
		return modelMapper.map( allianceMember , AllianceMemberDto.class );
	}

	public AllianceMemberDto getAllianceMemberDto( String discordMemberId ) {
		return toDtoFromEntity( allianceMemberRepository.findByDiscordMemberId( discordMemberId ) );
	}

	public AllianceMemberDto getAllianceMemberDtoByAyarabuName( String ayarabuName ) {
		AllianceMember allianceMember = allianceMemberRepository.findByAyarabuName( ayarabuName );
		AllianceMemberDto allianceMemberDto = modelMapper.map( allianceMember , AllianceMemberDto.class );
		List<ChatMessageView> chatMessageViewList = chatMessageViewRepository.findAllByMemberId( allianceMember.getId() );
		allianceMemberDto.setChatMessageViewList( chatMessageViewList );
		return allianceMemberDto;
	}

	public AllianceMemberDto getAllianceMemberDtoByMemberId( Integer memberId ) {
		AllianceMember allianceMember = allianceMemberRepository.findById( memberId ).get();
		AllianceMemberDto allianceMemberDto = modelMapper.map( allianceMember , AllianceMemberDto.class );
		List<ChatMessageView> chatMessageViewList = chatMessageViewRepository.findAllByMemberId( allianceMember.getId() );
		allianceMemberDto.setChatMessageViewList( chatMessageViewList );
		return allianceMemberDto;
	}

}
