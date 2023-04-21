package me.bartosz1.monitoring.services;

import me.bartosz1.monitoring.exceptions.EntityNotFoundException;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.models.WhiteLabelDomain;
import me.bartosz1.monitoring.models.WhiteLabelDomainCDO;
import me.bartosz1.monitoring.repositories.StatuspageRepository;
import me.bartosz1.monitoring.repositories.WhiteLabelDomainRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WhiteLabelDomainService {

    private final WhiteLabelDomainRepository whiteLabelDomainRepository;
    private final StatuspageRepository statuspageRepository;

    public WhiteLabelDomainService(WhiteLabelDomainRepository whiteLabelDomainRepository, StatuspageRepository statuspageRepository) {
        this.whiteLabelDomainRepository = whiteLabelDomainRepository;
        this.statuspageRepository = statuspageRepository;
    }

    public WhiteLabelDomain createWhiteLabelDomain(WhiteLabelDomainCDO cdo, User user) {
        //there's no validation of TLDs or anything like that because of local DNS and /etc/hosts
        return whiteLabelDomainRepository.save(new WhiteLabelDomain(cdo, user));
    }

    public WhiteLabelDomain deleteWhiteLabelDomain(long id, User user) throws EntityNotFoundException {
        Optional<WhiteLabelDomain> byId = whiteLabelDomainRepository.findById(id);
        if (byId.isPresent()) {
            WhiteLabelDomain whiteLabelDomain = byId.get();
            if (whiteLabelDomain.getUser().getId() == user.getId()) {
                //whiteLabelDomain.getStatuspage().setWhiteLabelDomain(null);
                whiteLabelDomainRepository.delete(whiteLabelDomain);
            }
        }
        throw new EntityNotFoundException("White label domain with ID " + id + " not found.");
    }

    public WhiteLabelDomain findWhiteLabelDomainById(long id, User user) throws EntityNotFoundException {
        Optional<WhiteLabelDomain> byId = whiteLabelDomainRepository.findById(id);
        if (byId.isPresent()) {
            WhiteLabelDomain whiteLabelDomain = byId.get();
            if (whiteLabelDomain.getUser().getId() == user.getId()) return whiteLabelDomain;
        }
        throw new EntityNotFoundException("White label domain with ID " + id + " not found.");
    }

    public Iterable<WhiteLabelDomain> findAllWhiteLabelDomains(User user) {
        return whiteLabelDomainRepository.findAllByUserId(user.getId());
    }

    public WhiteLabelDomain modifyWhiteLabelDomain(long id, User user, WhiteLabelDomainCDO cdo) throws EntityNotFoundException {
        Optional<WhiteLabelDomain> byId = whiteLabelDomainRepository.findById(id);
        if (byId.isPresent()) {
            WhiteLabelDomain whiteLabelDomain = byId.get();
            if (whiteLabelDomain.getUser().getId() == user.getId()) {
                WhiteLabelDomain afterEdit = new WhiteLabelDomain(cdo, user);
                afterEdit.setId(whiteLabelDomain.getId());
                afterEdit.setStatuspage(whiteLabelDomain.getStatuspage());
                return whiteLabelDomainRepository.save(afterEdit);
            }
        }
        throw new EntityNotFoundException("White label domain with ID " + id + " not found.");
    }

}
