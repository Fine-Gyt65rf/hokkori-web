package hokkori.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import hokkori.web.entity.ChannelMaster;

@Repository
public interface  ChannelMasterRepository extends JpaRepository<ChannelMaster, Integer>{
	public ChannelMaster findByChannelId(String channelId);
}
