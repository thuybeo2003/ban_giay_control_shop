package com.example.demo.buyerController;


import com.beust.ah.A;
import com.example.demo.model.*;
import com.example.demo.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/buyer")
public class UserController {

    @Autowired
    private HttpSession session;

    @Autowired
    private GHCTService ghctService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HoaDonChiTietService hoaDonChiTietService;

    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private DiaChiKHService diaChiKHService;

    @Autowired
    private ShippingFeeService shippingFeeService;

    @Autowired
    private GiaoHangService giaoHangService;

    @Autowired
    private LSThanhToanService lsThanhToanService;

    @Autowired
    private GiayChiTietService giayChiTietService;

    @Autowired
    private ViTriDonHangServices viTriDonHangServices;

    @Autowired
    private VNPayService vnPayService;

    @GetMapping("/setting")
    private String getSettingAccount(Model model){

        KhachHang khachHang = (KhachHang) session.getAttribute("KhachHangLogin");

        UserForm(model, khachHang);
        model.addAttribute("pagesettingAccount",true);
        return "online/user";
    }

    @GetMapping("/addresses")
    private String getAddressAccount(Model model){

        KhachHang khachHang = (KhachHang) session.getAttribute("KhachHangLogin");

        UserForm(model, khachHang);

        List<DiaChiKH> diaChiKHDefaultList = diaChiKHService.findbyKhachHangAndLoaiAndTrangThai(khachHang, true, 1);
        List<DiaChiKH> diaChiKHList = diaChiKHService.findbyKhachHangAndLoaiAndTrangThai(khachHang, false, 1);

        if (diaChiKHDefaultList.size() == 0){
            model.addAttribute("diaChiShowNull",true);
        }else{
            model.addAttribute("diaChiShow",true);
            model.addAttribute("addressKHDefault", diaChiKHDefaultList.get(0));
            model.addAttribute("listCartDetail", diaChiKHList);

        }
        model.addAttribute("pageAddressesUser",true);
        model.addAttribute("addNewAddressSetting",true);
        return "online/user";
    }

    @PostMapping("/addresses/add")
    private String addnewAddress(Model model,@RequestParam(name = "defaultSelected", defaultValue = "false") boolean defaultSelected){
        KhachHang khachHang = (KhachHang) session.getAttribute("KhachHangLogin");

        String nameAddress = request.getParameter("nameAddress");
        String fullName = request.getParameter("fullName");
        String phoneAddress = request.getParameter("phoneAddress");
        String city = request.getParameter("city");
        String district = request.getParameter("district");
        String ward = request.getParameter("ward");
        String description = request.getParameter("description");

        String diaChiChiTiet = description + ", " + ward + ", " + district + ", " + city;

        DiaChiKH diaChiKH = new DiaChiKH();
        diaChiKH.setDiaChiChiTiet(diaChiChiTiet);
        diaChiKH.setMoTa(description);
        diaChiKH.setKhachHang(khachHang);
        diaChiKH.setTrangThai(1);
        diaChiKH.setMaDC( "DC_" + khachHang.getMaKH() + generateRandomNumbers());
        diaChiKH.setSdtNguoiNhan(phoneAddress);
        diaChiKH.setQuanHuyen(district);
        diaChiKH.setTenDC(nameAddress);
        diaChiKH.setTinhTP(city);
        diaChiKH.setTenNguoiNhan(fullName);
        diaChiKH.setXaPhuong(ward);
        diaChiKH.setTgThem(new Date());
        diaChiKH.setLoai(defaultSelected);

        diaChiKHService.save(diaChiKH);

        return "redirect:/buyer/addresses";
    }

    @GetMapping("/addresses/delete/{idDC}")
    private String deleteAddress(Model model, @PathVariable UUID idDC){

        DiaChiKH diaChiKH =diaChiKHService.getByIdDiaChikh(idDC);
        diaChiKH.setTrangThai(0);
        diaChiKHService.save(diaChiKH);

        return "redirect:/buyer/addresses";
    }

    @GetMapping("/addresses/setDefault/{idDC}")
    private String setDefaultAddress(Model model, @PathVariable UUID idDC){
        KhachHang khachHang = (KhachHang) session.getAttribute("KhachHangLogin");

        DiaChiKH diaChiKH =diaChiKHService.getByIdDiaChikh(idDC);
        List<DiaChiKH> diaChiKHDefaultList = diaChiKHService.findbyKhachHangAndLoaiAndTrangThai(khachHang, true ,1);
        for (DiaChiKH x : diaChiKHDefaultList) {
            x.setLoai(false);
            diaChiKHService.save(x);
        }
        diaChiKH.setLoai(true);
        diaChiKHService.save(diaChiKH);

        return "redirect:/buyer/addresses";
    }

    @GetMapping("/purchase")
    private String getPurchaseAccount(Model model){

        KhachHang khachHang = (KhachHang) session.getAttribute("KhachHangLogin");
        GioHang gioHang = (GioHang) session.getAttribute("GHLogged") ;
        List<GioHangChiTiet> listGHCTActive = ghctService.findByGHActive(gioHang);
        List<HoaDon> listHoaDonByKhachHang = hoaDonService.findHoaDonByKhachHang(khachHang);
        List<HoaDon> listHoaDonChoThanhToan = hoaDonService.listHoaDonKhachHangAndTrangThaiOnline(khachHang, 0);
        Integer sumProductInCart = listGHCTActive.size();

        model.addAttribute("fullNameLogin", khachHang.getHoTenKH());
        model.addAttribute("sumProductInCart", sumProductInCart);
        model.addAttribute("pagePurchaseUser",true);
        model.addAttribute("purchaseAll",true);
        model.addAttribute("listAllHDByKhachHang", listHoaDonByKhachHang);
        model.addAttribute("listHoaDonChoThanhToan", listHoaDonChoThanhToan);
        model.addAttribute("type1","active");

        return "online/user";
    }

    @GetMapping("/purchase/pay")
    private String getPurchasePay(Model model){

        KhachHang khachHang = (KhachHang) session.getAttribute("KhachHangLogin");

        UserForm(model, khachHang);

        List<HoaDon> listHoaDonByKhachHang = hoaDonService.listHoaDonKhachHangAndTrangThaiOnline(khachHang, 0);

        model.addAttribute("listAllHDByKhachHang", listHoaDonByKhachHang);

        model.addAttribute("pagePurchaseUser",true);
        model.addAttribute("purchasePay",true);
        model.addAttribute("type2","active");
        return "online/user";
    }

    @GetMapping("/purchase/ship")
    private String getPurchaseShip(Model model){

        KhachHang khachHang = (KhachHang) session.getAttribute("KhachHangLogin");

        UserForm(model, khachHang);

        List<HoaDon> listHoaDonByKhachHang = hoaDonService.listHoaDonKhachHangAndTrangThaiOnline(khachHang, 1);


        model.addAttribute("listAllHDByKhachHang", listHoaDonByKhachHang);
        model.addAttribute("pagePurchaseUser",true);
        model.addAttribute("purchaseShip",true);
        model.addAttribute("type3","active");

        return "online/user";
    }

    @GetMapping("/purchase/receive")
    private String getPurchaseReceive(Model model){

        KhachHang khachHang = (KhachHang) session.getAttribute("KhachHangLogin");

        UserForm(model, khachHang);

        List<HoaDon> listHoaDonByKhachHang = hoaDonService.listHoaDonKhachHangAndTrangThaiOnline(khachHang, 2);

        model.addAttribute("listAllHDByKhachHang", listHoaDonByKhachHang);

        model.addAttribute("pagePurchaseUser",true);
        model.addAttribute("purchaseReceive",true);
        model.addAttribute("type4","active");
        return "online/user";
    }

    @GetMapping("/purchase/completed")
    private String getPurchaseCompleted(Model model){

        KhachHang khachHang = (KhachHang) session.getAttribute("KhachHangLogin");

        UserForm(model, khachHang);

        List<HoaDon> listHoaDonByKhachHang = hoaDonService.listHoaDonKhachHangAndTrangThaiOnline(khachHang, 3);

        model.addAttribute("listAllHDByKhachHang", listHoaDonByKhachHang);

        model.addAttribute("pagePurchaseUser",true);
        model.addAttribute("purchaseCompleted",true);
        model.addAttribute("type5","active");
        return "online/user";
    }

    @GetMapping("/purchase/cancel")
    private String getPurchaseCancel(Model model){

        KhachHang khachHang = (KhachHang) session.getAttribute("KhachHangLogin");

        UserForm(model, khachHang);

        List<HoaDon> listHoaDonByKhachHang = hoaDonService.listHoaDonKhachHangAndTrangThaiOnline(khachHang, 4);

        model.addAttribute("listAllHDByKhachHang", listHoaDonByKhachHang);

        model.addAttribute("pagePurchaseUser",true);
        model.addAttribute("purchaseCancel",true);
        model.addAttribute("type6","active");
        return "online/user";
    }

    @GetMapping("/purchase/refund")
    private String getPurchaseRefund(Model model){

        KhachHang khachHang = (KhachHang) session.getAttribute("KhachHangLogin");

        UserForm(model, khachHang);

        List<HoaDon> listHoaDonByKhachHang = hoaDonService.listHoaDonKhachHangAndTrangThaiOnline(khachHang, 3);

        model.addAttribute("listAllHDByKhachHang", listHoaDonByKhachHang);

        model.addAttribute("pagePurchaseUser",true);
        model.addAttribute("purchaseRefund",true);
        model.addAttribute("type7","active");
        return "online/user";
    }

    @GetMapping("/purchase/bill/detail/{idHD}")
    private String getDetailForm(Model model, @PathVariable UUID idHD){

        KhachHang khachHang = (KhachHang) session.getAttribute("KhachHangLogin");
        List<DiaChiKH> diaChiKHList = diaChiKHService.findbyKhachHangAndLoaiAndTrangThai(khachHang, false, 1);
        DiaChiKH diaChiKHDefault = diaChiKHService.findDCKHDefaulByKhachHang(khachHang);

        UserForm(model, khachHang);

        HoaDon hoaDon= hoaDonService.getOne(idHD);

        int trangThai = hoaDon.getTrangThai();
        if (trangThai == 0){

            Date ngayBatDau =  hoaDon.getTgTao();

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(ngayBatDau);

            // Thực hiện cộng thêm 30 ngày
            calendar.add(Calendar.DATE, 2);

            // Lấy ngày kết thúc
            Date ngayKetThuc = calendar.getTime();

            model.addAttribute("ngayKetThuc", ngayKetThuc);


            model.addAttribute("detailBillPay",true);
            model.addAttribute("modalThayDoiPhuongThucThanhToan",true);
            model.addAttribute("billDetailPay", hoaDon);

            session.removeAttribute("hoaDonPayDetail");
            session.setAttribute("hoaDonPayDetail", hoaDon);

        }else if (trangThai == 1){

            GiaoHang giaoHangListActive = hoaDon.getGiaoHang();
            model.addAttribute("giaoHangListActive", giaoHangListActive);
            model.addAttribute("modalThayDoiPhuongThucThanhToan",true);

            model.addAttribute("detailBillShip",true);
            model.addAttribute("billDetailShip", hoaDon);

            List<ViTriDonHang> viTriDonHangList = viTriDonHangServices.findByGiaoHang(giaoHangListActive);
            model.addAttribute("listViTriDonHang", viTriDonHangList);

            session.removeAttribute("hoaDonPayDetail");
            session.setAttribute("hoaDonPayDetail", hoaDon);

        }else if (trangThai == 2){

            GiaoHang giaoHangListActive = hoaDon.getGiaoHang();
            List<ViTriDonHang> viTriDonHangList = viTriDonHangServices.findByGiaoHang(giaoHangListActive);

            model.addAttribute("listViTriDonHang", viTriDonHangList);
            model.addAttribute("detailBillRecieve",true);
            model.addAttribute("billDetailRecieve", hoaDon);

        }else if (trangThai == 3){
            GiaoHang giaoHangListActive = hoaDon.getGiaoHang();

            Calendar calendar = Calendar.getInstance();

            calendar.add(Calendar.DATE, 2);

            Date ngayKetThuc = calendar.getTime();
            model.addAttribute("ngayKetThucHoanHang", ngayKetThuc);

            List<ViTriDonHang> viTriDonHangList = viTriDonHangServices.findByGiaoHang(giaoHangListActive);
            model.addAttribute("listViTriDonHang", viTriDonHangList);

            model.addAttribute("detailBillCompleted",true);
            model.addAttribute("billDetailCompleted", hoaDon);
        }else if (trangThai == 4){
            GiaoHang giaoHangListActive = hoaDon.getGiaoHang();

            List<ViTriDonHang> viTriDonHangList = viTriDonHangServices.findByGiaoHang(giaoHangListActive);
            model.addAttribute("listViTriDonHang", viTriDonHangList);
            model.addAttribute("detailBillCancel",true);
            model.addAttribute("billDetailCancel", hoaDon);

        }else if (trangThai == 5){
            GiaoHang giaoHangListActive = hoaDon.getGiaoHang();
            model.addAttribute("giaoHangListActive", giaoHangListActive);

            model.addAttribute("detailBillRefund",true);
            model.addAttribute("billDetailRefund", hoaDon);

        }

        model.addAttribute("listAddressKH", diaChiKHList);
        model.addAttribute("diaChiKHDefault", diaChiKHDefault);

        return "online/user";
    }

    @PostMapping("/purchaser/bill/refund/{idHD}")
    private String getDetailRefundForm(Model model, @PathVariable UUID idHD){

        KhachHang khachHang = (KhachHang) session.getAttribute("KhachHangLogin");
        List<DiaChiKH> diaChiKHList = diaChiKHService.findbyKhachHangAndLoaiAndTrangThai(khachHang, false, 1);
        DiaChiKH diaChiKHDefault = diaChiKHService.findDCKHDefaulByKhachHang(khachHang);

        UserForm(model, khachHang);

        HoaDon hoaDon= hoaDonService.getOne(idHD);

        session.removeAttribute("billRefundDetail");
        session.setAttribute("billRefundDetail", hoaDon);

        model.addAttribute("diaChiKHDefault", diaChiKHDefault);
        model.addAttribute("addNewAddressNotNull", true);
        model.addAttribute("listAddressKH", diaChiKHList);

        model.addAttribute("detailBillRefundMoney", true);
        model.addAttribute("billDetailRefund", hoaDon);

        session.removeAttribute("diaChiChiTiet");
        session.removeAttribute("sdtNguoiNhan");
        session.removeAttribute("hoTenNguoiNhan");

//        session.setAttribute("diaChiChiTiet", hoaDon.getDiaChiNguoiNhan());
//        session.setAttribute("sdtNguoiNhan", hoaDon.getSdtNguoiNhan());
//        session.setAttribute("hoTenNguoiNhan", hoaDon.getTenNguoiNhan());


        return "online/user";
    }

    @GetMapping("/purchase/bill/detail/cancel/{idHD}")
    private String getDetailCancelForm(Model model, @PathVariable UUID idHD){

        KhachHang khachHang = (KhachHang) session.getAttribute("KhachHangLogin");
        List<DiaChiKH> diaChiKHList = diaChiKHService.findbyKhachHangAndLoaiAndTrangThai(khachHang, false, 1);
        DiaChiKH diaChiKHDefault = diaChiKHService.findDCKHDefaulByKhachHang(khachHang);

        UserForm(model, khachHang);

        HoaDon hoaDon= hoaDonService.getOne(idHD);

        int trangThai = hoaDon.getTrangThai();
        if (trangThai == 0){
            model.addAttribute("modalHuyHoaDonInDetailBillPay",true);
            model.addAttribute("modalThayDoiPhuongThucThanhToan",true);

            model.addAttribute("detailBillPay",true);
            model.addAttribute("billDetailPay", hoaDon);

            session.removeAttribute("hoaDonPayDetail");
            session.setAttribute("hoaDonPayDetail", hoaDon);

        }else if (trangThai == 1){
            model.addAttribute("modalHuyHoaDonInDetailBillPay",true);

            GiaoHang giaoHangListActive = hoaDon.getGiaoHang();
            model.addAttribute("giaoHangListActive", giaoHangListActive);
            model.addAttribute("modalThayDoiPhuongThucThanhToan",true);

            model.addAttribute("detailBillShip",true);
            model.addAttribute("billDetailShip", hoaDon);

            session.removeAttribute("hoaDonPayDetail");
            session.setAttribute("hoaDonPayDetail", hoaDon);

        }else if (trangThai == 2){

            GiaoHang giaoHangListActive = hoaDon.getGiaoHang();
            model.addAttribute("giaoHangListActive", giaoHangListActive);

            model.addAttribute("detailBillRecieve",true);
            model.addAttribute("billDetailRecieve", hoaDon);

        }else if (trangThai == 3){
            GiaoHang giaoHangListActive = hoaDon.getGiaoHang();
            model.addAttribute("giaoHangListActive", giaoHangListActive);

            model.addAttribute("detailBillCompleted",true);
            model.addAttribute("billDetailCompleted", hoaDon);
        }else if (trangThai == 4){
            GiaoHang giaoHangListActive = hoaDon.getGiaoHang();
            model.addAttribute("giaoHangListActive", giaoHangListActive);

            model.addAttribute("detailBillCancel",true);
            model.addAttribute("billDetailCancel", hoaDon);

        }else if (trangThai == 5){
            GiaoHang giaoHangListActive = hoaDon.getGiaoHang();
            model.addAttribute("giaoHangListActive", giaoHangListActive);

            model.addAttribute("detailBillRefund",true);
            model.addAttribute("billDetailRefund", hoaDon);

        }

        model.addAttribute("listAddressKH", diaChiKHList);
        model.addAttribute("diaChiKHDefault", diaChiKHDefault);

        return "online/user";
    }

    @PostMapping("/purchase/pay/change/payment/{idHD}")
    private String changePaymentMethod(Model model, @PathVariable UUID idHD){

        HoaDon hoaDon = (HoaDon) session.getAttribute("hoaDonPayDetail");
        KhachHang khachHang = (KhachHang) session.getAttribute("KhachHangLogin");

        String hinhThucThayDoi = request.getParameter("paymentMethod");

        if (hinhThucThayDoi.equals("qrCodeBanking")){
            UserForm(model, khachHang);
            hoaDon.setHinhThucThanhToan(1);
            hoaDon.setTrangThai(0);
            hoaDonService.add(hoaDon);

            LichSuThanhToan lichSuThanhToan =  new LichSuThanhToan();
            lichSuThanhToan.setTgThanhToan(new Date());
            lichSuThanhToan.setSoTienThanhToan(hoaDon.getTongTien());
            lichSuThanhToan.setNoiDungThanhToan(hoaDon.getMaHD());
            lichSuThanhToan.setKhachHang(khachHang);
            lichSuThanhToan.setHoaDon(hoaDon);
            lichSuThanhToan.setMaLSTT("LSTT" + khachHang.getMaKH() + generateRandomNumbers());
            lichSuThanhToan.setTrangThai(0);
            lichSuThanhToan.setLoaiTT(0);
            lichSuThanhToan.setNoiDungThanhToan("Thay đổi hình thức thanh toán sang VNPAY");
            lsThanhToanService.addLSTT(lichSuThanhToan);

            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            double doubleNumber = hoaDon.getTongTien();
            int total = (int) doubleNumber;

            String vnpayUrl = vnPayService.createOrder(total, "orderInfo", baseUrl);
            hoaDon.setHinhThucThanhToan(1);
            hoaDon.setTrangThai(0);
            hoaDonService.add(hoaDon);

            session.removeAttribute("HoaDonThanhToanNhanNgu");
            session.setAttribute("HoaDonThanhToanNhanNgu", hoaDon);

            return "redirect:" + vnpayUrl;

        }else{
            UserForm(model, khachHang);

            hoaDon.setHinhThucThanhToan(0);
            hoaDon.setTrangThai(1);
            hoaDonService.add(hoaDon);

            return "redirect:/buyer/purchase/ship";
        }

    }

    @PostMapping("/purchase/bill/pay/cancel/{idHD}")
    private String cancelBillPay(Model model, @PathVariable UUID idHD){
        String lyDoHuy = request.getParameter("lyDoHuy");

        KhachHang khachHang = (KhachHang) session.getAttribute("KhachHangLogin");
        HoaDon hoaDonHuy = hoaDonService.getOne(idHD);

        LichSuThanhToan lichSuThanhToan =  new LichSuThanhToan();
        lichSuThanhToan.setTgThanhToan(new Date());
        lichSuThanhToan.setSoTienThanhToan(hoaDonHuy.getTongTien());
        lichSuThanhToan.setNoiDungThanhToan(hoaDonHuy.getMaHD());
        lichSuThanhToan.setKhachHang(khachHang);
        lichSuThanhToan.setHoaDon(hoaDonHuy);
        lichSuThanhToan.setMaLSTT("LSTT" + khachHang.getMaKH() + generateRandomNumbers());
        lichSuThanhToan.setLoaiTT(0);

         if(lyDoHuy.equals("changeProduct")){
            lyDoHuy = "Tôi muốn thay đổi sản phẩm";

            GiaoHang giaoHang = hoaDonHuy.getGiaoHang();
            giaoHang.setLyDoHuy(lyDoHuy);
            giaoHang.setTgHuy(new Date());
            giaoHangService.saveGiaoHang(giaoHang);

            if (hoaDonHuy.getHinhThucThanhToan() == 1){
                if (hoaDonHuy.getTrangThai() == 1){
                    lichSuThanhToan.setTrangThai(3);
                    lichSuThanhToan.setNoiDungThanhToan("Hủy đơn hàng đã thanh toán ");
                    lsThanhToanService.addLSTT(lichSuThanhToan);
                }else if(hoaDonHuy.getTrangThai() == 0){
                    lichSuThanhToan.setTrangThai(2);
                    lichSuThanhToan.setNoiDungThanhToan("Hủy đơn hàng chưa thanh toán ");
                    lsThanhToanService.addLSTT(lichSuThanhToan);
                }
            }

             hoaDonHuy.setTrangThai(4);
             hoaDonService.add(hoaDonHuy);
        }else if(lyDoHuy.equals("none")){
            lyDoHuy = "Tôi không  có nhu cầu mua nữa";
            GiaoHang giaoHang = hoaDonHuy.getGiaoHang();
            giaoHang.setLyDoHuy(lyDoHuy);
            giaoHang.setTgHuy(new Date());
            giaoHangService.saveGiaoHang(giaoHang);

             if (hoaDonHuy.getTrangThai() == 1){
                 lichSuThanhToan.setTrangThai(3);
                 lichSuThanhToan.setNoiDungThanhToan("Hủy đơn hàng đã thanh toán ");
                 lsThanhToanService.addLSTT(lichSuThanhToan);
             }else if(hoaDonHuy.getTrangThai() == 0){
                 lichSuThanhToan.setTrangThai(2);
                 lichSuThanhToan.setNoiDungThanhToan("Hủy đơn hàng chưa thanh toán ");
                 lsThanhToanService.addLSTT(lichSuThanhToan);
             }
             hoaDonHuy.setTrangThai(4);
             hoaDonService.add(hoaDonHuy);
        }else if (lyDoHuy.equals("lyDoKhac")) {
            lyDoHuy = request.getParameter("hutThuocNenDauDaDay");
            GiaoHang giaoHang = hoaDonHuy.getGiaoHang();
            giaoHang.setLyDoHuy(lyDoHuy);
            giaoHang.setTgHuy(new Date());
            giaoHangService.saveGiaoHang(giaoHang);

             if (hoaDonHuy.getTrangThai() == 1){
                 lichSuThanhToan.setTrangThai(3);
                 lichSuThanhToan.setNoiDungThanhToan("Hủy đơn hàng đã thanh toán ");
                 lsThanhToanService.addLSTT(lichSuThanhToan);
             }else if(hoaDonHuy.getTrangThai() == 0){
                 lichSuThanhToan.setTrangThai(2);
                 lichSuThanhToan.setNoiDungThanhToan("Hủy đơn hàng chưa thanh toán");
                 lsThanhToanService.addLSTT(lichSuThanhToan);
             }
             hoaDonHuy.setTrangThai(4);
             hoaDonService.add(hoaDonHuy);
        }else if(lyDoHuy.equals("changeSize")) {
            lyDoHuy = request.getParameter("hutThuocNenDauDaDay");


            GiaoHang giaoHang = hoaDonHuy.getGiaoHang();
            giaoHang.setLyDoHuy(lyDoHuy);
            giaoHang.setTgHuy(new Date());
            giaoHangService.saveGiaoHang(giaoHang);

             if (hoaDonHuy.getTrangThai() == 1){
                 lichSuThanhToan.setTrangThai(3);
                 lichSuThanhToan.setNoiDungThanhToan("Hủy đơn hàng đã thanh toán");
                 lsThanhToanService.addLSTT(lichSuThanhToan);
             }else if(hoaDonHuy.getTrangThai() == 0){
                 lichSuThanhToan.setTrangThai(2);
                 lichSuThanhToan.setNoiDungThanhToan("Hủy đơn hàng chưa thanh toán");
                 lsThanhToanService.addLSTT(lichSuThanhToan);
             }
             hoaDonHuy.setTrangThai(4);
             hoaDonService.add(hoaDonHuy);
        }
    return "redirect:/buyer/purchase/cancel";

    }

    @GetMapping("/purchase/bill/pay/{idHD}")
    private String payBill(Model model, @PathVariable UUID idHD){
        HoaDon hoaDon = hoaDonService.getOne(idHD);
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        double doubleNumber = hoaDon.getTongTien();
        int total = (int) doubleNumber;

        String vnpayUrl = vnPayService.createOrder(total, "orderInfo", baseUrl);
        hoaDon.setHinhThucThanhToan(1);
        hoaDon.setTrangThai(0);
        hoaDonService.add(hoaDon);

        session.removeAttribute("HoaDonThanhToanNhanNgu");
        session.setAttribute("HoaDonThanhToanNhanNgu", hoaDon);

        return "redirect:" + vnpayUrl;

    }

    @GetMapping("/purchaser/bill/buy/again/{idHD}")
    private String buyAgain(Model model, @PathVariable UUID idHD){
        HoaDon hoaDonBuyAgain = hoaDonService.getOne(idHD);

        List<HoaDonChiTiet> hoaDonChiTietList = hoaDonChiTietService.findByHoaDon(hoaDonBuyAgain);

        GioHang gioHang = (GioHang) session.getAttribute("GHLogged") ;

        for (HoaDonChiTiet x:hoaDonChiTietList) {

            GioHangChiTiet gioHangChiTietExist = ghctService.findByCTSPActive(x.getChiTietGiay());

            if (gioHangChiTietExist != null){

                gioHangChiTietExist.setSoLuong(gioHangChiTietExist.getSoLuong() + x.getSoLuong());
                gioHangChiTietExist.setTgThem(new Date());
                gioHangChiTietExist.setDonGia(x.getSoLuong()*x.getChiTietGiay().getGiaBan() + gioHangChiTietExist.getDonGia());
                ghctService.addNewGHCT(gioHangChiTietExist);

            }else {

                GioHangChiTiet gioHangChiTiet = new GioHangChiTiet();

                gioHangChiTiet.setGioHang(gioHang);
                gioHangChiTiet.setDonGia(x.getSoLuong() * x.getChiTietGiay().getGiaBan());
                gioHangChiTiet.setChiTietGiay(x.getChiTietGiay());
                gioHangChiTiet.setSoLuong(x.getSoLuong());
                gioHangChiTiet.setTrangThai(1);
                gioHangChiTiet.setTgThem(new Date());

                ghctService.addNewGHCT(gioHangChiTiet);
            }
        }

        return "redirect:/buyer/cart";
    }

    @PostMapping("/purchase/bill/add/address/get/{idHD}")
    private String addAddressNhanHang(Model model,
                                      @PathVariable UUID idHD,
                                      @RequestParam(name = "defaultSelected", defaultValue = "false") boolean defaultSelected){

        HoaDon hoaDon = hoaDonService.getOne(idHD);
        KhachHang khachHang = (KhachHang) session.getAttribute("KhachHangLogin");

        String nameAddress = request.getParameter("nameAddress");
        String fullName = request.getParameter("fullName");
        String phoneAddress = request.getParameter("phoneAddress");
        String city = request.getParameter("city");
        String district = request.getParameter("district");
        String ward = request.getParameter("ward");
        String description = request.getParameter("description");

        String diaChiChiTiet = description + ", " + ward + ", " + district + ", " + city;

        DiaChiKH diaChiKH = new DiaChiKH();
        diaChiKH.setDiaChiChiTiet(diaChiChiTiet);
        diaChiKH.setMoTa(description);
        diaChiKH.setKhachHang(khachHang);
        diaChiKH.setTrangThai(1);
        diaChiKH.setMaDC( "DC_" + khachHang.getMaKH() + generateRandomNumbers());
        diaChiKH.setSdtNguoiNhan(phoneAddress);
        diaChiKH.setQuanHuyen(district);
        diaChiKH.setTenDC(nameAddress);
        diaChiKH.setTinhTP(city);
        diaChiKH.setTenNguoiNhan(fullName);
        diaChiKH.setXaPhuong(ward);
        diaChiKH.setTgThem(new Date());
        diaChiKH.setLoai(defaultSelected);

        diaChiKHService.save(diaChiKH);

//        hoaDon.setTenNguoiNhan(diaChiKH.getTenNguoiNhan());
//        hoaDon.setSdtNguoiNhan(diaChiKH.getSdtNguoiNhan());
//        hoaDon.setDiaChiNguoiNhan(diaChiKH.getDiaChiChiTiet());

        hoaDonService.add(hoaDon);

        Double tienShip = shippingFeeService.calculatorShippingFee(hoaDon, 25000.0);

        double tienShipCu = hoaDon.getTienShip();

        Date ngayBatDau =  hoaDon.getTgTao();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ngayBatDau);

        calendar.add(Calendar.DATE, shippingFeeService.tinhNgayNhanDuKien(hoaDon));

        hoaDon.setTongTien(hoaDon.getTongTien() - tienShipCu + tienShip);
        hoaDon.setTienShip(tienShip);
//        hoaDon.setTgNhanDK(calendar.getTime());
        hoaDon.setSoLanThayDoiViTriShip(1);
//        hoaDon.setTongTienDG(hoaDon.getTongTienDG() + tienShip - tienShipCu);

        hoaDonService.add(hoaDon);


        UserForm(model, khachHang);

        List<HoaDon> listHoaDonByKhachHang = hoaDonService.findHoaDonByKhachHang(khachHang);

        List<HoaDon> listHoaDonChoThanhToan = hoaDonService.listHoaDonKhachHangAndTrangThaiOnline(khachHang, 0);

        model.addAttribute("pagePurchaseUser",true);
        model.addAttribute("purchaseAll",true);
        model.addAttribute("listAllHDByKhachHang", listHoaDonByKhachHang);
        model.addAttribute("listHoaDonChoThanhToan", listHoaDonChoThanhToan);

        model.addAttribute("type1","active");

        model.addAttribute("showThongBaoThayDoiDiaChiNhanHangThanhCong", true);

        return "online/user";
    }

    @PostMapping("/purchase/bill/change/address/get/{idHD}")
    private String changeAddressNhanHang(Model model, @PathVariable UUID idHD){
        HoaDon hoaDon = hoaDonService.getOne(idHD);
        KhachHang khachHang = (KhachHang) session.getAttribute("KhachHangLogin");

        UUID idDCKH = UUID.fromString(request.getParameter("idDCKH"));

        DiaChiKH diaChiKH = diaChiKHService.findByIdDiaChiKH(idDCKH);

//        hoaDon.setTenNguoiNhan(diaChiKH.getTenNguoiNhan());
//        hoaDon.setSdtNguoiNhan(diaChiKH.getSdtNguoiNhan());
//        hoaDon.setDiaChiNguoiNhan(diaChiKH.getDiaChiChiTiet());

        hoaDonService.add(hoaDon);

        Double tienShip = shippingFeeService.calculatorShippingFee(hoaDon, 25000.0);

        double tienShipCu = hoaDon.getTienShip();

        Date ngayBatDau =  hoaDon.getTgTao();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ngayBatDau);

        calendar.add(Calendar.DATE, shippingFeeService.tinhNgayNhanDuKien(hoaDon));

        hoaDon.setTongTien(hoaDon.getTongTien() - tienShipCu + tienShip);
        hoaDon.setTienShip(tienShip);
        hoaDon.setSoLanThayDoiViTriShip(1);

        hoaDonService.add(hoaDon);

        UserForm(model, khachHang);
        List<HoaDon> listHoaDonByKhachHang = hoaDonService.findHoaDonByKhachHang(khachHang);
        List<HoaDon> listHoaDonChoThanhToan = hoaDonService.listHoaDonKhachHangAndTrangThaiOnline(khachHang, 0);

        model.addAttribute("pagePurchaseUser",true);
        model.addAttribute("purchaseAll",true);
        model.addAttribute("listAllHDByKhachHang", listHoaDonByKhachHang);
        model.addAttribute("listHoaDonChoThanhToan", listHoaDonChoThanhToan);

        model.addAttribute("type1","active");
        model.addAttribute("showThongBaoThayDoiDiaChiNhanHangThanhCong", true);

        return "online/user";
    }

    private void UserForm(Model model, KhachHang khachHang){
        GioHang gioHang = (GioHang) session.getAttribute("GHLogged") ;
        model.addAttribute("fullNameLogin", khachHang.getHoTenKH());

        List<GioHangChiTiet> listGHCTActive = ghctService.findByGHActive(gioHang);
        Integer sumProductInCart = listGHCTActive.size();
        model.addAttribute("sumProductInCart", sumProductInCart);
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


}

