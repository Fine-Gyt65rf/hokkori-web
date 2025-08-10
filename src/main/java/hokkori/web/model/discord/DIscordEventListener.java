package hokkori.web.model.discord;

import hokkori.web.dto.AllianceMemberDto;
import hokkori.web.dto.ChatMessageDto;

public interface DIscordEventListener {
	public void onMessageReceived(ChatMessageDto chatMessageDto) ;
	public void onMessageUpdate(ChatMessageDto chatMessageDto) ;
	public void onMessageDelete(String messageId) ;
	public void onGuildMemberJoin(AllianceMemberDto allianceMemberDto) ;
	public void onGuildMemberRemove(AllianceMemberDto allianceMemberDto) ;

}
