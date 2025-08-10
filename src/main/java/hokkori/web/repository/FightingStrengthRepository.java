package hokkori.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import hokkori.web.entity.FightingStrength;

@Repository
public interface FightingStrengthRepository extends JpaRepository<FightingStrength, Integer>{

}
