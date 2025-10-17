package com.example.crewstation.controller.admin;

import com.example.crewstation.auth.CustomUserDetails;
import com.example.crewstation.domain.notice.NoticeDetailVO;
import com.example.crewstation.domain.payment.PaymentVO;
import com.example.crewstation.dto.member.MemberAdminStatics;
import com.example.crewstation.dto.member.MemberCriteriaDTO;
import com.example.crewstation.dto.member.MemberDTO;
import com.example.crewstation.dto.notice.NoticeCriteriaDTO;
import com.example.crewstation.dto.notice.NoticeWriteRequest;
import com.example.crewstation.dto.payment.status.PaymentCriteriaDTO;
import com.example.crewstation.dto.report.post.ReportPostDTO;
import com.example.crewstation.repository.payment.PaymentDAO;
import com.example.crewstation.repository.payment.status.PaymentStatusDAO;
import com.example.crewstation.service.banner.BannerService;
import com.example.crewstation.service.member.MemberService;
import com.example.crewstation.service.notice.NoticeDetailService;
import com.example.crewstation.service.notice.NoticeService;
import com.example.crewstation.service.payment.PaymentService;
import com.example.crewstation.service.gift.GiftService;
import com.example.crewstation.service.report.ReportService;
import com.example.crewstation.util.Criteria;
import com.example.crewstation.util.Search;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminRestController {

    private final MemberService memberService;
    private final NoticeService noticeService;
    private final NoticeDetailService noticeDetailService;
    private final ReportService reportService;
    private final GiftService giftService;
    private final PaymentService paymentService;
    private final PaymentDAO paymentDAO;
    private final PaymentStatusDAO paymentStatusDAO;

    //    관리자 회원 목록
    @PostMapping("/members")
    public ResponseEntity<MemberCriteriaDTO> getMembers(@RequestBody Search search) {
        return ResponseEntity.ok(memberService.getMembers(search));
    }

    //    관리자 회원 상세
    @GetMapping("/members/{memberId}")
    public ResponseEntity<MemberDTO> getMemberDetails(@PathVariable Long memberId) {
        return ResponseEntity.ok(memberService.getMemberDetail(memberId));
    }

    //    관리자 메인 회원 통계
    @GetMapping({"", "/"})
    public ResponseEntity<MemberAdminStatics> getStatics() {
        MemberAdminStatics statics = memberService.getStatics();
        return ResponseEntity.ok(statics);

    }

    //  공지사항 목록
    @GetMapping("/notices")
    public ResponseEntity<NoticeCriteriaDTO> getAdminNotices(@RequestParam(defaultValue = "1") int page) {
        int safePage = Math.max(1, page);
        return ResponseEntity.ok(noticeService.getAdminNotices(safePage));
    }

    //    공지사항 작성
    @PostMapping("/notices")
    public ResponseEntity<?> createNotice(@AuthenticationPrincipal CustomUserDetails admin,
                                          @RequestBody NoticeWriteRequest req) {
        Long memberId = admin.getId();
        Long id = noticeService.insertNotice(memberId, req.getTitle(), req.getContent());
        if (id == null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(new HashMap<>());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }


    //    공지사항 상세
    @GetMapping("/notices/{id}")
    public ResponseEntity<NoticeDetailVO> noticeDetail(@PathVariable Long id) {
        NoticeDetailVO notice = noticeDetailService.getDetail(id);
        return ResponseEntity.ok(notice);
    }

    //    다이어리 신고 목록
    @GetMapping("/diaries")
    public ResponseEntity<?> getReportDiaryList(@RequestParam(defaultValue = "1") int page) {
        int safePage = Math.max(1, page);
        List<ReportPostDTO> reports = reportService.getReportDiaries(safePage);
        return ResponseEntity.ok(reports);
    }

    //    다이어리 신고 처리
    @PostMapping("/diary/{reportId}/process")
    public ResponseEntity<?> processDiaryReport(@PathVariable Long reportId, @RequestParam(required = false) Long postId, @RequestParam(defaultValue = "false") boolean hidePost) {

        log.info("다이어리 신고 reportId={}, postId={}, hidePost={}", reportId, postId, hidePost);

        if (hidePost && postId != null) {
            reportService.hidePost(postId);
        }

        reportService.resolveReport(reportId);

        return ResponseEntity.ok().build();
    }

    //    기프트 신고 목록
    @GetMapping("/gifts")
    public ResponseEntity<?> getReportGiftList(@RequestParam(defaultValue = "1") int page) {
        int safePage = Math.max(1, page);
        List<ReportPostDTO> reports = giftService.getReportGifts(safePage);
        return ResponseEntity.ok(reports);
    }

    //    기프트 신고 처리
    @PostMapping("/gift/{reportId}/process")
    public ResponseEntity<?> processGiftReport(@PathVariable Long reportId, @RequestParam(required = false) Long postId, @RequestParam(defaultValue = "false") boolean hidePost) {

        log.info("기프트 신고 reportId={}, postId={}, hidePost={}", reportId, postId, hidePost);

        if (hidePost && postId != null) {
            reportService.hidePost(postId);
        }

        reportService.resolveReport(reportId);

        return ResponseEntity.ok().build();
    }
    
    //    결제 목록
    @GetMapping("/payment")
    public ResponseEntity<?> getPayment(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String categories,
            @RequestParam(required = false) String keyword) {

        Search search = new Search();
        search.setPage(Math.max(1, page));
        if (keyword != null) search.setKeyword(keyword);

        if (categories != null && !categories.isBlank()) {
            List<String> cats = Arrays.stream(categories.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            if (cats.size() == 3) {
                search.setCategories(null);
            } else {
                search.setCategories(cats);
            }
        }

        int size = 16;
        List<PaymentCriteriaDTO> list = paymentService.selectPayment(search, size);
        return ResponseEntity.ok(list);
    }


    //  결제 목록 상세 보기
    @GetMapping("/payment/{id}")
    public ResponseEntity<PaymentCriteriaDTO> getPaymentDetail(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentDetail(id));
    }



}
