package com.example.demo.controller;

import com.beust.ah.A;
import com.example.demo.model.*;
import com.example.demo.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RequestMapping("/manage/bill/")
@Controller
public class  HoaDonOnlineController {

    @Autowired
    private HoaDonChiTietService hoaDonChiTietService;

    @Autowired
    private GiaoHangService giaoHangService;

    @Autowired
    private GiayChiTietService giayChiTietService;

    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private NhanVienService nhanVienService;

    @Autowired
    private HttpSession session;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private LSThanhToanService lsThanhToanService;

    @Autowired
    private ViTriDonHangServices viTriDonHangServices;

    @Autowired
    private ChucVuService chucVuService;

    @GetMapping("online")
    private String manageBillOnline(Model model) {
        model.addAttribute("reLoadPage", true);
        showData(model);
        showTab1(model);
        showThongBao(model);

        return "manage/manage-bill-online";
    }

    @GetMapping("online/delivery/{idHD}")
    private String editBillOnline(Model model, @PathVariable UUID idHD) {

        HoaDon billDelivery = hoaDonService.getOne(idHD);
        List<NhanVien> listNhanVienGiao = new ArrayList<>();

        ChucVu quanLi = chucVuService.findByMaCV("CV01");
        ChucVu nvgh = chucVuService.findByMaCV("CV03");

        List<NhanVien> listQL = nhanVienService.findByChucVu(quanLi);
        List<NhanVien> listNVGH = nhanVienService.findByChucVu(nvgh);

        if (listQL != null) {
            for (NhanVien x : listQL) {
                listNhanVienGiao.add(x);
            }
        }
        if (listNVGH != null) {
            for (NhanVien x : listNVGH) {
                listNhanVienGiao.add(x);
            }
        }

        model.addAttribute("listNhanVienGiao", listNhanVienGiao);
        model.addAttribute("billDelivery", billDelivery);
        model.addAttribute("showEditBillDelivery", true);

        showTab3(model);

        showData(model);
        showThongBao(model);

        return "manage/manage-bill-online";
    }

    @PostMapping("online/delivery/confirm/{idHD}")
    private String confirmNVGH(Model model, @PathVariable UUID idHD) {

        UUID idNV = UUID.fromString(request.getParameter("idNhanVien"));

        Date date = new Date();
        NhanVien nhanVien = nhanVienService.getByIdNhanVien(idNV);
        HoaDon hoaDon = hoaDonService.getOne(idHD);

        hoaDon.setNhanVien(nhanVien);
        hoaDon.setTrangThai(2);

        hoaDonService.add(hoaDon);

        GiaoHang giaoHang = hoaDon.getGiaoHang();
        giaoHang.setNoiDung("Xác nhận nhân viên giao hàng");
        giaoHang.setTgShip(date);
        giaoHangService.saveGiaoHang(giaoHang);

        hoaDon.setGiaoHang(giaoHang);
        hoaDonService.add(hoaDon);

        ViTriDonHang viTriDonHang = new ViTriDonHang();
        viTriDonHang.setGiaoHang(giaoHang);
        viTriDonHang.setViTri("Xác nhận nhân viên giao hàng");
        viTriDonHang.setTrangThai(1);
        viTriDonHang.setThoiGian(new Date());
        viTriDonHangServices.addViTriDonHang(viTriDonHang);

        showData(model);

        model.addAttribute("showMessThanhCong", true);
        model.addAttribute("reLoadPage", true);
        showData(model);
        showTab3(model);
        showThongBao(model);

        return "manage/manage-bill-online";
    }

    @PostMapping("online/confirm/pay/{idHD}")
    private String confirmPayBill(Model model, @PathVariable UUID idHD) {

        Date date = new Date();

        String noiDungThanhToan = request.getParameter("noiDungThanhToan");

        HoaDon hoaDonThanhToan = hoaDonService.getOne(idHD);

        String thoiGianThanhToan = request.getParameter("thoiGianThanhToan");

        Date thoiGianThanhToanFormat = new Date();

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        try {
            thoiGianThanhToanFormat = inputFormat.parse(thoiGianThanhToan);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String maLSTT = "HTT0" + date.getDay() + generateRandomNumbers();

        LichSuThanhToan lichSuThanhToan = new LichSuThanhToan();

        lichSuThanhToan.setHoaDon(hoaDonThanhToan);
        lichSuThanhToan.setTgThanhToan(new Date());
//        lichSuThanhToan.setSoTienThanhToan(hoaDonThanhToan.getTongTienDG());
        lichSuThanhToan.setTrangThai(1);
        lichSuThanhToan.setKhachHang(hoaDonThanhToan.getKhachHang());
        lichSuThanhToan.setMaLSTT(maLSTT);
        lichSuThanhToan.setLoaiTT(0); //Thanh toán online
        lichSuThanhToan.setNoiDungThanhToan(noiDungThanhToan);
        lsThanhToanService.addLSTT(lichSuThanhToan);

        hoaDonThanhToan.setTrangThai(1);
//        hoaDonThanhToan.setTgThanhToan(thoiGianThanhToanFormat);
        hoaDonService.add(hoaDonThanhToan);

        showData(model);
        showTab2(model);
        showThongBao(model);

        model.addAttribute("reLoadPage", true);

        return "manage/manage-bill-online";
    }

    @GetMapping("/online/print/{idHD}")
    private String printBillOnline(Model model, @PathVariable UUID idHD) {

        HoaDon hoaDon = hoaDonService.getOne(idHD);

        model.addAttribute("billPrint", hoaDon);

        return "manage/managePrintBill";
    }

    @PostMapping("online/refund/delivery/confirm/{idHD}")
    private String confirmNVGHBillRefund(Model model, @PathVariable UUID idHD) {

        UUID idNV = UUID.fromString(request.getParameter("idNhanVien"));

        Date date = new Date();
        NhanVien nhanVien = nhanVienService.getByIdNhanVien(idNV);
        HoaDon hoaDon = hoaDonService.getOne(idHD);

        hoaDon.setNhanVien(nhanVien);
        hoaDon.setTrangThai(2);
//        hoaDon.setTgShip(date);

        hoaDonService.add(hoaDon);

        String maGH = "GH_" + hoaDon.getMaHD();

        GiaoHang giaoHang = new GiaoHang();
        giaoHang.setHoaDon(hoaDon);
        giaoHang.setTrangThai(1);
        giaoHang.setMaGiaoHang(maGH);
        giaoHang.setThoiGian(new Date());
        giaoHang.setPhiGiaoHang(0.0);
        giaoHang.setNoiDung("Xác nhận nhân viên hoàn hàng");
        giaoHangService.saveGiaoHang(giaoHang);

        hoaDon.setGiaoHang(giaoHang);
        hoaDonService.add(hoaDon);

        ViTriDonHang viTriDonHang = new ViTriDonHang();
        viTriDonHang.setGiaoHang(giaoHang);
        viTriDonHang.setViTri("Xác nhận nhân viên hoàn hàng");
        viTriDonHang.setTrangThai(1);
        viTriDonHang.setThoiGian(new Date());
        viTriDonHangServices.addViTriDonHang(viTriDonHang);

        showData(model);

        model.addAttribute("showMessThanhCong", true);
        model.addAttribute("reLoadPage", true);
        showData(model);
        showTab3(model);
        showThongBao(model);

        return "manage/manage-bill-online";
    }

    @GetMapping("online/bill/refund/delivery/{idHD}")
    private String editBillRefundOnline(Model model, @PathVariable UUID idHD) {

        HoaDon billDelivery = hoaDonService.getOne(idHD);
        List<NhanVien> listNhanVienGiao = new ArrayList<>();

        ChucVu quanLi = chucVuService.findByMaCV("CV01");
        ChucVu nvgh = chucVuService.findByMaCV("CV03");

        List<NhanVien> listQL = nhanVienService.findByChucVu(quanLi);
        List<NhanVien> listNVGH = nhanVienService.findByChucVu(nvgh);

        if (listQL != null) {
            for (NhanVien x : listQL) {
                listNhanVienGiao.add(x);
            }
        }
        if (listNVGH != null) {
            for (NhanVien x : listNVGH) {
                listNhanVienGiao.add(x);
            }
        }

        model.addAttribute("listNhanVienGiao", listNhanVienGiao);
        model.addAttribute("billDelivery", billDelivery);
        model.addAttribute("showEditBillRefundDelivery", true);

        showTab4(model);

        showData(model);

        return "manage/manage-bill-online";
    }

    @GetMapping("/online/print/refund/bill/{idHD}")
    private String printBillRefundOnline(Model model, @PathVariable UUID idHD) {

        HoaDon hoaDon = hoaDonService.getOne(idHD);

        model.addAttribute("billPrint", hoaDon);

        return "manage/managePrintBillRefund";
    }

    @PostMapping("online/cancel/refund/{idHD}")
    private String confirmRefundBillCancel(Model model, @PathVariable UUID idHD) {
        HoaDon hoaDon = hoaDonService.getOne(idHD);

        Date date = new Date();
        String maLSTT = "HTT0" + date.getDay() + generateRandomNumbers();

        LichSuThanhToan lichSuThanhToan = new LichSuThanhToan();
        lichSuThanhToan.setHoaDon(hoaDon);
        lichSuThanhToan.setTgThanhToan(new Date());
//        lichSuThanhToan.setSoTienThanhToan(hoaDon.getTongTienDG());
        lichSuThanhToan.setTrangThai(1);
        lichSuThanhToan.setKhachHang(hoaDon.getKhachHang());
        lichSuThanhToan.setMaLSTT(maLSTT);
        lichSuThanhToan.setLoaiTT(0); //Thanh toán online
        lichSuThanhToan.setNoiDungThanhToan("Thanh toán cho hóa đơn hủy thành công");
        lsThanhToanService.addLSTT(lichSuThanhToan);

        model.addAttribute("reLoadPage", true);
        model.addAttribute("thongBaoHoanTien", true);
        showData(model);
        showTab1(model);
        showThongBao(model);

        return "manage/manage-bill-online";
    }

    @PostMapping("online/refund/refund/{idHD}")
    private String confirmRefundBillRefund(Model model, @PathVariable UUID idHD){
        HoaDon hoaDon = hoaDonService.getOne(idHD);
//        HoaDon hoaDonOld = hoaDonService.findByIdHoaDonOld(hoaDon.getIdHDOld());

        Date date = new Date();
        String maLSTT = "HTT0" + date.getDay() + generateRandomNumbers();

        LichSuThanhToan lichSuThanhToan = new LichSuThanhToan();
        lichSuThanhToan.setHoaDon(hoaDon);
        lichSuThanhToan.setTgThanhToan(new Date());
//        lichSuThanhToan.setSoTienThanhToan(hoaDon.getTongTienDG());
        lichSuThanhToan.setTrangThai(1);
        lichSuThanhToan.setKhachHang(hoaDon.getKhachHang());
        lichSuThanhToan.setMaLSTT(maLSTT);
        lichSuThanhToan.setLoaiTT(0); //Thanh toán online
        lichSuThanhToan.setNoiDungThanhToan("Đã hoàn lại tiền cho khách hàng");
        lsThanhToanService.addLSTT(lichSuThanhToan);

//        hoaDon.setTrangThaiHoan(8);
//        hoaDonOld.setTrangThaiHoan(8);
//
//        PhieuTraHang phieuTraHang = hoaDon.getPhieuTraHang();
//
//        phieuTraHang.setTrangThai(8);
//
//        phieuTraHangServices.savePTH(phieuTraHang);
        hoaDonService.add(hoaDon);
//        hoaDonService.add(hoaDonOld);

        model.addAttribute("thongBaoHoanTien", true);
        model.addAttribute("reLoadPage", true);
        showData(model);
        showTab1(model);
        showThongBao(model);

        return "manage/manage-bill-online";
    }

    private void showThongBao(Model model){
    }

    private void showData(Model model){

        List<HoaDon> listAllHoaDonOnline = hoaDonService.listHoaDonOnline();


        List<HoaDon> listHoaDonHoan = new ArrayList<>();

        List<HoaDon> listAllHoaDonOnline2 = new ArrayList<>();
        List<HoaDon> listAllHoaDonDangGiao = new ArrayList<>();
        List<HoaDon> listHoaDonOnlineQRCode = new ArrayList<>();


        int soLuongHoaDonHoan = 0;
        int soLuongHoaDonOnline = 0;
        int soLuongHoaDonHuy = 0;
        int soLuongHoaDonDaThanhToan = 0 ;
        int soLuongHoaDonChuaThanhToanQRCode = 0 ;
        int soLuongHoaDonChuaThanhToanNhanHang = 0 ;
        int soLuongHoaDonDangGiao = 0;
        int soLuongHoaDonBanking = 0;
        int soLuongHoaDonDaNhan = 0;

        int soLuongHoaDonThanhToanKhiNhanHang = 0;

        double tongTienHoaDon = 0.0;
        if (listAllHoaDonOnline != null) {
            for (HoaDon x : listAllHoaDonOnline) {
                if (x.getTrangThai() == 6 || x.getTrangThai() == 7) {
                    System.out.println("abc");
                } else {
                    tongTienHoaDon += x.getTongTien();
                }
            }
        }

        if (listAllHoaDonOnline != null){
            for (HoaDon x: listAllHoaDonOnline) {
                if (x.getTrangThai() == 6 || x.getTrangThai() == 7){
                    System.out.println("abc");
                }else{
//                    if (x.getIdHDOld() != null){
//                        soLuongHoaDonHoan ++;
//                        listHoaDonHoan.add(x);
//                    }
//                    if (x.getIdHDOld() == null){
//                        listAllHoaDonOnline2.add(x);
//                        soLuongHoaDonOnline ++;
//                    }
                    if(x.getTrangThai() == 4){
                        soLuongHoaDonHuy ++;
                    }
                    if (x.getHinhThucThanhToan() == 1 ){
                        soLuongHoaDonBanking ++;
                        listHoaDonOnlineQRCode.add(x);
                    }
                    if (x.getHinhThucThanhToan() == 0){
                        soLuongHoaDonThanhToanKhiNhanHang ++;
                    }
                    if (x.getHinhThucThanhToan() == 1 && x.getTrangThai() == 0){
                        soLuongHoaDonChuaThanhToanQRCode ++;
                    }
                    if ( x.getTrangThai() == 1 || x.getTrangThai() == 2){
                        if (x.getHinhThucThanhToan() == 0){
                            soLuongHoaDonChuaThanhToanQRCode ++;
                        }
                    }
                    if (x.getTrangThai() == 1 && x.getHinhThucThanhToan() == 1 ){
                        soLuongHoaDonDaThanhToan ++;
                    }
                    if (x.getTrangThai() == 3 && x.getHinhThucThanhToan() == 0 ){
                        soLuongHoaDonDaThanhToan ++;
                    }
                    if (x.getTrangThai() == 2 ){
                        soLuongHoaDonDangGiao ++;
                        listAllHoaDonDangGiao.add(x);
                    }
                    if (x.getTrangThai() == 1){
                        soLuongHoaDonDangGiao ++;
                        listAllHoaDonDangGiao.add(x);
                    }
                    if (x.getTrangThai() == 4){
                        soLuongHoaDonHuy ++;
                    }
                    if (x.getTrangThai() == 3){
                        soLuongHoaDonDaNhan ++;
                    }
                }
            }
        }

        int soLuongHoaDonChuaThanhToan =  soLuongHoaDonChuaThanhToanNhanHang +  soLuongHoaDonChuaThanhToanQRCode;

        double tongTienHoan = 0.0;
        int soLuongSPHoan = 0;
        int soLuongHdHoanChoXacNhan = 0;
        int soLuongHdHoanKhachHuy = 0;
        int soLuongHdHoanDangHoan = 0;
        int soLuongHdHoanChuaHoanTien = 0;
        int soLuongHdHoanDaHoanTien = 0;
        int soLuongHdHoanTuChoi = 0;


        model.addAttribute("sumBillOnline", soLuongHoaDonOnline);
        model.addAttribute("totalAmount", tongTienHoaDon);
        model.addAttribute("sumQuantityQRCode", soLuongHoaDonBanking);
        model.addAttribute("sumQuantityDelivery", soLuongHoaDonThanhToanKhiNhanHang);
        model.addAttribute("hoaDonChuaThanhToan", soLuongHoaDonChuaThanhToan);
        model.addAttribute("hoaDonDaThanhToan", soLuongHoaDonDaThanhToan);
        model.addAttribute("hoaDonDangGiao", soLuongHoaDonDangGiao);
        model.addAttribute("hoaDonHuy", soLuongHoaDonHuy);
        model.addAttribute("hoaDonDaNhan", soLuongHoaDonDaNhan);
        model.addAttribute("hoaDonDaHoan", soLuongHoaDonHoan);

        model.addAttribute("tongTienHoan", tongTienHoan);
        model.addAttribute("soLuongSPHoan", soLuongSPHoan);
        model.addAttribute("soLuongHdHoanChoXacNhan", soLuongHdHoanChoXacNhan);
        model.addAttribute("soLuongHdHoanKhachHuy", soLuongHdHoanKhachHuy);
        model.addAttribute("soLuongHdHoanDangHoan", soLuongHdHoanDangHoan);
        model.addAttribute("soLuongHdHoanChuaHoanTien", soLuongHdHoanChuaHoanTien);
        model.addAttribute("soLuongHdHoanDaHoanTien", soLuongHdHoanDaHoanTien);
        model.addAttribute("soLuongHdHoanTuChoi", soLuongHdHoanTuChoi);

        model.addAttribute("listBillRefund", listHoaDonHoan);
        model.addAttribute("listHoaDonOnline", listAllHoaDonOnline);
        model.addAttribute("listHoaDonOnlineQRCode", listHoaDonOnlineQRCode);
        model.addAttribute("listHoaDonOnlineGiaoHang", listAllHoaDonDangGiao);
    }

    public String generateRandomNumbers() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int randomNumber = random.nextInt(10); // Tạo số ngẫu nhiên từ 0 đến 9
            sb.append(randomNumber);
        }
        return sb.toString();
    }

    private void showTab1(Model model){
        model.addAttribute("activeAll", "nav-link active");
        model.addAttribute("xac_nhan_tt", "nav-link");
        model.addAttribute("van_chuyen", "nav-link");
        model.addAttribute("hoan_hang", "nav-link");

        model.addAttribute("tabpane1", "tab-pane show active");
        model.addAttribute("tabpane2", "tab-pane");
        model.addAttribute("tabpane3", "tab-pane");
        model.addAttribute("tabpane4", "tab-pane");
    }

    private void showTab2(Model model){

        model.addAttribute("activeAll", "nav-link");
        model.addAttribute("xac_nhan_tt", "nav-link active");
        model.addAttribute("van_chuyen", "nav-link");
        model.addAttribute("hoan_hang", "nav-link");

        model.addAttribute("tabpane1", "tab-pane");
        model.addAttribute("tabpane2", "tab-pane show active");
        model.addAttribute("tabpane3", "tab-pane");
        model.addAttribute("tabpane4", "tab-pane");
    }

    private void showTab3(Model model){

        model.addAttribute("activeAll", "nav-link");
        model.addAttribute("xac_nhan_tt", "nav-link");
        model.addAttribute("van_chuyen", "nav-link active");
        model.addAttribute("hoan_hang", "nav-link");

        model.addAttribute("tabpane1", "tab-pane");
        model.addAttribute("tabpane2", "tab-pane");
        model.addAttribute("tabpane3", "tab-pane show active");
        model.addAttribute("tabpane4", "tab-pane");
    }

    private void showTab4(Model model){

        model.addAttribute("activeAll", "nav-link");
        model.addAttribute("xac_nhan_tt", "nav-link");
        model.addAttribute("van_chuyen", "nav-link");
        model.addAttribute("hoan_hang", "nav-link active");

        model.addAttribute("tabpane1", "tab-pane");
        model.addAttribute("tabpane2", "tab-pane");
        model.addAttribute("tabpane3", "tab-pane");
        model.addAttribute("tabpane4", "tab-pane show active");

        }
}
