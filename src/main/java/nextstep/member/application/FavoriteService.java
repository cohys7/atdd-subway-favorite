package nextstep.member.application;

import nextstep.auth.principal.UserPrincipal;
import nextstep.member.application.dto.FavoriteRequest;
import nextstep.member.application.exception.ErrorCode;
import nextstep.member.application.exception.FavoriteException;
import nextstep.member.domain.Favorite;
import nextstep.member.domain.FavoriteRepository;
import nextstep.member.domain.Member;
import nextstep.subway.applicaion.PathService;
import nextstep.subway.applicaion.StationService;
import nextstep.subway.domain.Station;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final StationService stationService;
    private final MemberService memberService;
    private final PathService pathService;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           StationService stationService,
                           MemberService memberService,
                           PathService pathService) {
        this.favoriteRepository = favoriteRepository;
        this.stationService = stationService;
        this.memberService = memberService;
        this.pathService = pathService;
    }

    @Transactional
    public Favorite create(UserPrincipal userPrincipal, FavoriteRequest favoriteRequest) {
        try {
            pathService.findPath(favoriteRequest.getSource(), favoriteRequest.getTarget());
        } catch (Exception e) {
            throw new FavoriteException(ErrorCode.CANNOT_ADD_NOT_EXIST_PATH);
        }

        Member member = memberService.findMemberByEmail(userPrincipal.getUsername());
        Station source = stationService.findById(favoriteRequest.getSource());
        Station target = stationService.findById(favoriteRequest.getTarget());
        Favorite favorite = favoriteRepository.save(new Favorite(source, target));
        member.addFavorite(favorite);
        return favorite;
    }

    public List<Favorite> find(UserPrincipal userPrincipal) {
        Member member = memberService.findMemberByEmail(userPrincipal.getUsername());
        return member.getFavorites();
    }

    @Transactional
    public void delete(UserPrincipal userPrincipal, Long id) {
        Member member = memberService.findMemberByEmail(userPrincipal.getUsername());
        member.deleteFavorite(id);
    }
}