package com.dwenc.cmas.id.controller;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import jcf.data.GridData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dwenc.cmas.common.user.domain.Sign;
import com.dwenc.cmas.common.user.service.SignUserService;
import com.dwenc.cmas.common.utils.CallBackServlet;
import com.dwenc.cmas.common.utils.RequestUtil;
import com.dwenc.cmas.common.utils.SignProcess;
import com.dwenc.cmas.id.domain.IdAppn;
import com.dwenc.cmas.id.domain.IdAppnDtl;
import com.dwenc.cmas.id.domain.IdAppnSys;
import com.dwenc.cmas.id.domain.IdAppnSysDtl;
import com.dwenc.cmas.id.service.IdAppnService;

import com.dwenc.cmas.common.utils.RexUtil;

import docfbaro.common.StringUtil;
import docfbaro.common.WebContext;
import docfbaro.iam.UserInfo;
import docfbaro.sua.exception.BusinessException;
import docfbaro.sua.mvc.MciRequest;
import docfbaro.sua.mvc.MciResponse;

@Controller
@RequestMapping("/id/idAppn/*")
public class IdAppnController {

	/**
	 * Logger 객체 생성
	 */
	private static Logger logger = LoggerFactory.getLogger(IdAppnController.class);

	/**
	 * 해당 Controller와 연결된 Service Class
	 */
    @Autowired
    private IdAppnService sService;

    @Autowired
    private SignUserService suService;

    @Autowired
    private SignProcess signProcess;

    /**
     * Common Message 처리
     */
    @Autowired
    private MessageSourceAccessor messageSourceAccessor;

    @RequestMapping("getUserId.*")
	public void getUserId(MciRequest request, MciResponse response) {
		Map<String, Object> map = RequestUtil.getParam(request, WebContext.getRequest());

		String userId = (String) sService.getUserId();
		map.put("userId", userId);
		response.setMap("ds_Result", map);
	}

    /**
     * 조회
     * @param request
     * @param response
     */
    @RequestMapping("getDocNo.*")
	public void getDocNo(MciRequest request, MciResponse response) {
		Map<String, Object> map = RequestUtil.getParam(request, WebContext.getRequest());
		GridData<IdAppnDtl> gridIdAppnDtlList = request.getGridData("idAppnDtlList", IdAppnDtl.class);

		String docNo = (String) sService.getDocNo(map).get("docNo");
		map.put("docNo", docNo);
		map.put("fstRegUserId", UserInfo.getUserId());
		sService.saveIdAppn(map, gridIdAppnDtlList);

		List<IdAppnSysDtl> ds_IdAppnSysDtl = sService.retrieveIdAppnSysDtlList();
		response.setList("ds_IdAppnSysDtl", ds_IdAppnSysDtl);
		response.setMap("ds_Result", map);
	}

    @RequestMapping("SignCallBack.*")
	public void signCallBack(MciRequest request, MciResponse response) {
    	Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> data = request.get("json", HashMap.class);
		Map<String, Object> result = new HashMap<String, Object>();
		String docStsCd = data.get("docStsCd").toString();
		map.put("signId", data.get("signId").toString());
		map.put("signUserId", data.get("signUserId").toString());
		map.put("signSeq", data.get("signSeq").toString());
		map.put("docNo", data.get("docNo").toString());
		map.put("docSts", docStsCd); 		// 업무의 결재문서상태 업데이트

		if(docStsCd.equals("D02"))			// 결재중
			map.put("signStsCd", "S04");
		else if(docStsCd.equals("D03"))		// 결재완료
			map.put("signStsCd", "S03");
		else if (docStsCd.equals("D04")) 	// 반려
			map.put("signStsCd", "S05");
		else if (docStsCd.equals("D11")) {	// 협의요청
			if(!data.get("signSeq").toString().equals("2"))	// 협의
				map.put("signStsCd", "S04");
			else
				map.put("signStsCd", "S11");
		}
		else if (docStsCd.equals("D12")) {	// 협의완료
			map.put("signStsCd", "S04");
		}

		result = sService.sign(map, data);

		response.setMap("RESULT", result);
    }

    @RequestMapping("draftIdAppn.*")
    public void draftIdAppn(MciRequest request, MciResponse response){
    	Map<String, Object> map = RequestUtil.getParam(request, WebContext.getRequest());
		Map<String, Object> rMap = new HashMap<String, Object>();
		GridData<Sign> gridSign = request.getGridData("signln", Sign.class);
		GridData<IdAppnDtl> gridOriIdAppnDtlList = request.getGridData("oriIdAppnDtlList", IdAppnDtl.class);
		GridData<IdAppnDtl> gridIdAppnDtlList = request.getGridData("idAppnDtlList", IdAppnDtl.class);
		GridData<IdAppnSys> gridIdAppnSysBaronet = request.getGridData("idAppnSysBaronet", IdAppnSys.class);
		GridData<IdAppnSys> gridIdAppnSysMail = request.getGridData("idAppnSysMail", IdAppnSys.class);
		GridData<IdAppnSys> gridIdAppnSysBaromi = request.getGridData("idAppnSysBaromi", IdAppnSys.class);
		GridData<IdAppnSys> gridIdAppnSysBarocon = request.getGridData("idAppnSysBarocon", IdAppnSys.class);
		GridData<IdAppnSys> gridIdAppnSysSVPN = request.getGridData("idAppnSysSVPN", IdAppnSys.class);
		GridData<IdAppnSys> gridIdAppnSysMobile = request.getGridData("idAppnSysMobile", IdAppnSys.class);
		GridData<IdAppnSys> gridIdAppnSysDRM = request.getGridData("idAppnSysDRM", IdAppnSys.class);
		GridData<IdAppnSys> gridIdAppnSysPOMS = request.getGridData("idAppnSysPOMS", IdAppnSys.class);
		GridData<IdAppnSys> gridIdAppnSysITS = request.getGridData("idAppnSysITS", IdAppnSys.class);
		GridData<IdAppnSys> gridIdAppnSysSafety = request.getGridData("idAppnSysSafety", IdAppnSys.class); //// CWH 똑바로 안전 권한 추가 20220729


		/**
		 * signProcess.draft required map info:
		 * userId		: 사용자 		ex) 1204594
		 * orgCd		: 조직코드		ex) 1DFUR
		 * dutySysCd	: 업무시스템코드 	ex) SGNS
		 * programCode	: 결재양식코드 	ex) SGNS070003
		 * signDocTitle : 결재문서제목	ex) 제목... URLEncoder.encode("제목", "utf-8")
		 * legacyInfo	: etc(Rexpert등)	ex) key:value||key:value||... URLEncoder.encode("key:value||", "utf-8"));
		 * signInfo		: 결재선정보		ex) 1DFUR^1204594^1^T01##1DFUR^1202564^2^T02##1DFUR^1304252^3^T03##1DFUR^1203160^4^T02
		 * process 		: 콜백URL		ex) http://iworks.daewooenc.com/cmas/id/idAppn/SignCallBack.xpl
		 *
		 */

		// 결재상신 처리
		try{
			/**
			 * 운영/개발 분기 처리
			 */
			HttpServletRequest req = WebContext.getRequest();
			String serverType = req.getServerName();
			String serverURL = "";
			if 	(serverType.equals("iworks.daewooenc.com"))	serverURL = "https://iworks.daewooenc.com";	// 운영
			//else serverURL = "http://172.23.20.113:8080"; //// 로컬 테스트
			else serverURL = "https://test.daewooenc.com";	// 개발

			/**
			 * 결재선 정보 생성(통합결재 전송용)
			 */
			String signInfo = "";
			for(int i=0; i<gridSign.size(); i++){
				signInfo = signInfo + gridSign.get(i).getApperOrgCd() + "^" + gridSign.get(i).getSignUserId() + "^"
						+ gridSign.get(i).getSignSeq() + "^" + gridSign.get(i).getSignTpCd() + "##";
			}
			/**
			 * 제목 정보 생성 (통합결재 전송용)
			 */

			String signDocTitle_code = "";
			if(map.get("programCode").toString().equals("SGNS070004")) {	//신규
				signDocTitle_code = "특별ID 신규 신청";
			} else if(map.get("programCode").toString().equals("SGNS070006")) { //연장
				signDocTitle_code = "특별ID 연장 신청";
			} else if(map.get("programCode").toString().equals("SGNS070007")) { //해지
				signDocTitle_code = "특별ID 해지 신청";
			} else { //수정, 기타
				signDocTitle_code = "특별ID";
			}

			String signDocTitle = "특별ID";
			if(gridOriIdAppnDtlList.size() == 1)
				signDocTitle = signDocTitle_code + " : " + gridOriIdAppnDtlList.get(0).getUserId();
			else if(gridOriIdAppnDtlList.size() > 1)
				signDocTitle = signDocTitle_code + " : " + gridOriIdAppnDtlList.get(0).getUserId() + " 외 " + (gridOriIdAppnDtlList.size()-1) + "건";
//			else
//				throw new Exception();

			/**
			 * userId : idAppnDraft.js 로 부터 옴
			 * orgCd : idAppnDraft.js 로 부터 옴
			 */
			map.put("useSignForm", "Y");	//양식정보의 협의자 사용여부
			map.put("dutySysCd", "SGNS");
			map.put("programCode", StringUtil.getText(map.get("programCode")));
			map.put("signDocTitle", URLEncoder.encode(signDocTitle, "utf-8"));
			map.put("legacyInfo", URLEncoder.encode("docNo:" + (String)map.get("docNo"), "utf-8"));
			map.put("signInfo", signInfo);
			map.put("process", serverURL + "/cmas/id/idAppn/SignCallBack.xpl");
//			map.put("process", "http://172.21.18.201:9090" + "/cmas/id/idAppn/SignCallBack.xpl");
			map.put("invokeURL", serverURL + "/sgns/sn/sign/remoteDraft.xpl");

			rMap = signProcess.draft(map);
			/**
			 * 상신 성공 시, 처리
			 */
			if ( ((String)rMap.get("TYPE")).equals("SUCCESS") ) {
				map.put("docSts", "D02");
				for(int i=0; i<gridSign.size(); i++){
					if(gridSign.get(i).getSignSeq().equals("1")){
						gridSign.get(i).setSignStsCd("S01");
						gridSign.get(i).setSignDt("now");
					}
					gridSign.get(i).setSignId((String)rMap.get("signId"));
				}
				sService.saveIdAppn(map, gridIdAppnDtlList);
				sService.saveIdAppnSys(map, gridIdAppnSysBaronet);
				sService.saveIdAppnSys(map, gridIdAppnSysMail);
				sService.saveIdAppnSys(map, gridIdAppnSysBaromi);
				sService.saveIdAppnSys(map, gridIdAppnSysBarocon);

				//테스트중 (SVPN 권한은 결재시 저장)
				if(StringUtil.getText(map.get("programCode")).toString().equals("SGNS070008")) { //수정 (SVPN 권한 부여 신청)
					sService.saveIdAppnSys2(map, gridIdAppnSysSVPN);	//update되도록 처리
				}else{
					sService.saveIdAppnSys(map, gridIdAppnSysSVPN);
				}

				sService.saveIdAppnSys(map, gridIdAppnSysMobile);
				sService.saveIdAppnSys(map, gridIdAppnSysDRM);
				sService.saveIdAppnSys(map, gridIdAppnSysPOMS);
				sService.saveIdAppnSys(map, gridIdAppnSysITS);
				sService.saveIdAppnSys(map, gridIdAppnSysSafety); //// CWH 똑바로 안전 권한 추가 20220729

				//// 똑바로 안전(웹) 권한 추가시 모바일 권한 동시 부여 (화면에서 해당 권한 신청항목은 보이지 않기 하기 위함)
				for (int i = 0; i < gridIdAppnSysSafety.size(); i++) {
					if(gridIdAppnSysSafety.get(i).getPrivCd().equals("0")) {
						gridIdAppnSysSafety.get(i).setSysCd("11");
						gridIdAppnSysSafety.get(i).setPrivCd("0");
	//					sService.saveIdAppnSys(map,gridIdAppnSysSafety);
					}else{gridIdAppnSysSafety.get(i).setSysCd("11");
						gridIdAppnSysSafety.get(i).setPrivCd("RO_SSFM_SF_003");
					}
				}

				sService.saveIdAppnSys(map, gridIdAppnSysSafety);

				suService.insertSign(gridSign);
			}else{
				throw new Exception();
			}
		}catch(Exception e){
			/**
			 * 상신 실패 시, 처리
			 */
			throw new BusinessException(null, (String)rMap.get("MSG_VALUES"), null);
		}
    }

    /**
     * 저장
     * @param request
     * @param response
     */
    @RequestMapping("saveIdAppn.*")
	public void saveIdAppn(MciRequest request, MciResponse response) {
		Map<String, Object> map = RequestUtil.getParam(request, WebContext.getRequest());
		GridData<Sign> gridSign = request.getGridData("signln", Sign.class);
		GridData<IdAppnDtl> gridIdAppnDtlList = request.getGridData("idAppnDtlList", IdAppnDtl.class);
		GridData<IdAppnSys> gridIdAppnSysBaronet = request.getGridData("idAppnSysBaronet", IdAppnSys.class);
		GridData<IdAppnSys> gridIdAppnSysMail = request.getGridData("idAppnSysMail", IdAppnSys.class);
		GridData<IdAppnSys> gridIdAppnSysBaromi = request.getGridData("idAppnSysBaromi", IdAppnSys.class);
		GridData<IdAppnSys> gridIdAppnSysBarocon = request.getGridData("idAppnSysBarocon", IdAppnSys.class);
		GridData<IdAppnSys> gridIdAppnSysSVPN = request.getGridData("idAppnSysSVPN", IdAppnSys.class);
		GridData<IdAppnSys> gridIdAppnSysMobile = request.getGridData("idAppnSysMobile", IdAppnSys.class);
		GridData<IdAppnSys> gridIdAppnSysDRM = request.getGridData("idAppnSysDRM", IdAppnSys.class);
		GridData<IdAppnSys> gridIdAppnSysPOMS = request.getGridData("idAppnSysPOMS", IdAppnSys.class);
		GridData<IdAppnSys> gridIdAppnSysITS = request.getGridData("idAppnSysITS", IdAppnSys.class);
		GridData<IdAppnSys> gridIdAppnSysSafety = request.getGridData("idAppnSysSafety", IdAppnSys.class);//// CWH 똑바로 안전 권한 추가 20220729


		sService.saveIdAppn(map, gridIdAppnDtlList);
		sService.saveIdAppnSys(map, gridIdAppnSysBaronet);
		sService.saveIdAppnSys(map, gridIdAppnSysMail);
		sService.saveIdAppnSys(map, gridIdAppnSysBaromi);
		sService.saveIdAppnSys(map, gridIdAppnSysBarocon);

		//테스트중 (SVPN 권한은 결재시 저장)
		if(StringUtil.getText(map.get("programCode")).toString().equals("SGNS070008")) { //수정 (SVPN 권한 부여 신청)
			sService.saveIdAppnSys2(map, gridIdAppnSysSVPN);	//update되도록 처리
		}else{
			sService.saveIdAppnSys(map, gridIdAppnSysSVPN);
		}

		sService.saveIdAppnSys(map, gridIdAppnSysMobile);
		sService.saveIdAppnSys(map, gridIdAppnSysDRM);
		sService.saveIdAppnSys(map, gridIdAppnSysPOMS);
		sService.saveIdAppnSys(map, gridIdAppnSysITS);
		sService.saveIdAppnSys(map, gridIdAppnSysSafety); //// CWH 똑바로 안전 권한 추가 20220729


		//// 똑바로 안전(웹) 권한 추가시 모바일 권한 동시 부여 (화면에서 해당 권한 신청항목은 보이지 않기 하기 위함)
		for(int i= 0; i < gridIdAppnSysSafety.size(); i++) {
			if(gridIdAppnSysSafety.get(i).getPrivCd().equals("0")) {
				gridIdAppnSysSafety.get(i).setSysCd("11");
				gridIdAppnSysSafety.get(i).setPrivCd("0");
			}else{gridIdAppnSysSafety.get(i).setSysCd("11");
				gridIdAppnSysSafety.get(i).setPrivCd("RO_SSFM_SF_003");
			}
		}
		sService.saveIdAppnSys(map, gridIdAppnSysSafety);
		//// 똑바로 안전(웹) 권한 추가시 모바일 권한 동시 부여 (화면에서 해당 권한 신청항목은 보이지 않기 하기 위함)
		suService.insertSign(gridSign);

		String docSts = StringUtil.getText(map.get("docSts"));
		if(docSts.equals("D03")) {
			sService.saveAfterFinish(map);
		}
	}

    /**
     * 마스터 조회
     * @param request
     * @param response
     */
    @RequestMapping("retrieveIdAppnList.*")
	public void retrieveIdAppnList(MciRequest request, MciResponse response) {
		Map<String, Object> map = RequestUtil.getParam(request, WebContext.getRequest());

		List<IdAppn> ds_IdAppnList = sService.retrieveIdAppnList(map);
		response.setList("ds_IdAppnList", ds_IdAppnList);  		//Map을 Client로 전송
	}

    /**
     * 상세 조회
     * @param request
     * @param response
     */
    @RequestMapping("retrieveIdAppnDtlList.*")
	public void retrieveIdAppnDtlList(MciRequest request, MciResponse response) {
		Map<String, Object> map = RequestUtil.getParam(request, WebContext.getRequest());

		List<IdAppnDtl> ds_IdAppnDtlList = sService.retrieveIdAppnDtlList(map);

		ArrayList userIdList = new ArrayList();
		for(int i=0; i<ds_IdAppnDtlList.size(); i++)
			userIdList.add(ds_IdAppnDtlList.get(i).getUserId());
		map.put("userIdList", userIdList);

		map.put("sysCd", "01");
		List<IdAppnSys> ds_IdAppnSysBaronet = sService.retrieveIdAppnSys(map);
		map.put("sysCd", "04");
		List<IdAppnSys> ds_IdAppnSysMail = sService.retrieveIdAppnSys(map);
		map.put("sysCd", "03");
		List<IdAppnSys> ds_IdAppnSysBaromi = sService.retrieveIdAppnSys(map);
		map.put("sysCd", "02");
		List<IdAppnSys> ds_IdAppnSysBarocon = sService.retrieveIdAppnSys(map);
		map.put("sysCd", "05");
		List<IdAppnSys> ds_IdAppnSysSVPN = sService.retrieveIdAppnSys(map);
		map.put("sysCd", "06");
		List<IdAppnSys> ds_IdAppnSysMobile = sService.retrieveIdAppnSys(map);
		map.put("sysCd", "07");
		List<IdAppnSys> ds_IdAppnSysDRM = sService.retrieveIdAppnSys(map);
		map.put("sysCd", "08");
		List<IdAppnSys> ds_IdAppnSysPOMS = sService.retrieveIdAppnSys(map);
		map.put("sysCd", "09");
		List<IdAppnSys> ds_IdAppnSysITS = sService.retrieveIdAppnSys(map);
		map.put("sysCd", "10");
		List<IdAppnSys> ds_IdAppnSysSafety = sService.retrieveIdAppnSys(map);  //// CWH 똑바로 안전 권한 추가 20220729

		List<IdAppnSysDtl> ds_IdAppnSysDtl = sService.retrieveIdAppnSysDtlList();

		response.setList("ds_IdAppnDtlList", ds_IdAppnDtlList);  		//Map을 Client로 전송
		response.setList("ds_IdAppnSysBaronet", ds_IdAppnSysBaronet);  	//Map을 Client로 전송
		response.setList("ds_IdAppnSysMail", ds_IdAppnSysMail);  		//Map을 Client로 전송
		response.setList("ds_IdAppnSysBaromi", ds_IdAppnSysBaromi);  	//Map을 Client로 전송
		response.setList("ds_IdAppnSysBarocon", ds_IdAppnSysBarocon);  	//Map을 Client로 전송
		response.setList("ds_IdAppnSysSVPN", ds_IdAppnSysSVPN);  		//Map을 Client로 전송
		response.setList("ds_IdAppnSysMobile", ds_IdAppnSysMobile);  	//Map을 Client로 전송
		response.setList("ds_IdAppnSysDRM", ds_IdAppnSysDRM); 	 		//Map을 Client로 전송
		response.setList("ds_IdAppnSysPOMS", ds_IdAppnSysPOMS);  		//Map을 Client로 전송
		response.setList("ds_IdAppnSysITS", ds_IdAppnSysITS); 	 		//Map을 Client로 전송
		response.setList("ds_IdAppnSysSafety", ds_IdAppnSysSafety);		//Map을 Client로 전송 //// CWH 똑바로 안전 권한 추가 20220729
		response.setList("ds_IdAppnSysDtl", ds_IdAppnSysDtl);
	}

    /**
     * 시스템 조회
     * @param request
     * @param response
     */
    @RequestMapping("retrieveIdAppnSysDtlList.*")
	public void retrieveIdAppnSysDtlList(MciRequest request, MciResponse response) {
		List<IdAppnSysDtl> ds_IdAppnSysDtlList = sService.retrieveIdAppnSysDtlList();
		response.setList("ds_IdAppnSysDtlList", ds_IdAppnSysDtlList);  		//Map을 Client로 전송
	}

    /**
     * 연장 신청 대상 조회
     * @param request
     * @param response
     */
    @RequestMapping("retrieveUserIdList.*")
	public void retrieveUserIdList(MciRequest request, MciResponse response) {
    	Map<String, Object> map = RequestUtil.getParam(request, WebContext.getRequest());
		List<IdAppnDtl> ds_IdAppnDtlList = sService.retrieveUserIdList(map);
		response.setList("ds_IdAppnDtlList", ds_IdAppnDtlList);  		//Map을 Client로 전송
	}

    @RequestMapping("retrieveUserIdSysList.*")
	public void retrieveUserIdSysList(MciRequest request, MciResponse response) {
    	Map<String, Object> map = RequestUtil.getParam(request, WebContext.getRequest());
    	GridData<IdAppnDtl> gridSign = request.getGridData("idAppnDtlList", IdAppnDtl.class);

    	ArrayList userIdList = new ArrayList();
		for(int i=0; i<gridSign.size(); i++)
			userIdList.add(gridSign.get(i).getUserId());
		map.put("userIdList", userIdList);

		map.put("sysCd", "01");
		List<IdAppnSys> ds_IdAppnSysBaronet = sService.retrieveIdAppnSys(map);
		map.put("sysCd", "04");
		List<IdAppnSys> ds_IdAppnSysMail = sService.retrieveIdAppnSys(map);
		map.put("sysCd", "03");
		List<IdAppnSys> ds_IdAppnSysBaromi = sService.retrieveIdAppnSys(map);
		map.put("sysCd", "02");
		List<IdAppnSys> ds_IdAppnSysBarocon = sService.retrieveIdAppnSys(map);
		map.put("sysCd", "05");
		List<IdAppnSys> ds_IdAppnSysSVPN = sService.retrieveIdAppnSys(map);
		map.put("sysCd", "06");
		List<IdAppnSys> ds_IdAppnSysMobile = sService.retrieveIdAppnSys(map);
		map.put("sysCd", "07");
		List<IdAppnSys> ds_IdAppnSysDRM = sService.retrieveIdAppnSys(map);
		map.put("sysCd", "08");
		List<IdAppnSys> ds_IdAppnSysPOMS = sService.retrieveIdAppnSys(map);
		map.put("sysCd", "09");
		List<IdAppnSys> ds_IdAppnSysITS = sService.retrieveIdAppnSys(map);
		map.put("sysCd", "10");
		List<IdAppnSys> ds_IdAppnSysSafety = sService.retrieveIdAppnSys(map); //// CWH 똑바로 안전 권한 추가 20220729

		List<IdAppnSysDtl> ds_IdAppnSysDtl = sService.retrieveIdAppnSysDtlList();

		response.setList("ds_IdAppnSysBaronet", ds_IdAppnSysBaronet);  		//Map을 Client로 전송
		response.setList("ds_IdAppnSysMail", ds_IdAppnSysMail);  			//Map을 Client로 전송
		response.setList("ds_IdAppnSysBaromi", ds_IdAppnSysBaromi);  		//Map을 Client로 전송
		response.setList("ds_IdAppnSysBarocon", ds_IdAppnSysBarocon);  		//Map을 Client로 전송
		response.setList("ds_IdAppnSysSVPN", ds_IdAppnSysSVPN);  			//Map을 Client로 전송
		response.setList("ds_IdAppnSysMobile", ds_IdAppnSysMobile);  		//Map을 Client로 전송
		response.setList("ds_IdAppnSysDRM", ds_IdAppnSysDRM);	  			//Map을 Client로 전송
		response.setList("ds_IdAppnSysPOMS", ds_IdAppnSysPOMS);	  			//Map을 Client로 전송
		response.setList("ds_IdAppnSysITS", ds_IdAppnSysITS);	  			//Map을 Client로 전송
		response.setList("ds_IdAppnSysSafety", ds_IdAppnSysSafety);		//Map을 Client로 전송 //// CWH 똑바로 안전 권한 추가 20220729
		response.setList("ds_IdAppnSysDtl", ds_IdAppnSysDtl);
	}

    /**
     * 시스템 담당자 저장
     * @param request
     * @param response
     */
    @RequestMapping("saveIdAppnSysDtl.*")
	public void saveIdAppnSysDtl(MciRequest request, MciResponse response) {
		Map<String, Object> map = RequestUtil.getParam(request, WebContext.getRequest());
		GridData<IdAppnSysDtl> gridIdAppnSysDtlList = request.getGridData("idAppnSysDtlList", IdAppnSysDtl.class);

		sService.saveIdAppnSysDtl(map, gridIdAppnSysDtlList);
	}

    /**
     * 확인 기능
     * @param request
     * @param response
     */
    @RequestMapping("confirmIdAppnDtl.*")
	public void confirmIdAppnDtl(MciRequest request, MciResponse response) {
		Map<String, Object> map = RequestUtil.getParam(request, WebContext.getRequest());

		sService.confirmIdAppnDtl(map);
	}

    /**
     * 종료 안내 메일
     * SVPN : 종료일자 다음날 발송
     * ID전체 : 1달전 발송
     * @param request
     * @param response
     */
    @RequestMapping("expInfo.*")
	public void expInfo(final MciRequest request, MciResponse response) throws Exception {
    	Map<String, Object> map = RequestUtil.getParam(request, WebContext.getRequest());
    	Map<String,Object> result = new HashMap<String, Object>();
		result = sService.sendMailForExpId();	//ID종료 30일전
		result = sService.sendMailForExpId10();	//ID종료 10일전
		result = sService.sendMailForExpId5();	//ID종료  5일전
		result = sService.sendMailToSVPNManager();	// SVPN
		response.setMap("RESULT", result);
	}
}

