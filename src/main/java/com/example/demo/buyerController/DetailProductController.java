package com.example.demo.buyerController;

import com.example.demo.model.*;
import com.example.demo.service.*;
import com.example.demo.viewModel.CTGViewModel;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
@RequestMapping("/buyer")
public class DetailProductController {

    @Autowired
    private HttpSession session;

    @Autowired
    private GiayChiTietService giayChiTietService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private GiayService giayService;

    @Autowired
    private GHCTService ghctService;

    @Autowired
    private CTGViewModelService ctgViewModelService;

    @Autowired
    private MauSacService mauSacService;

    @GetMapping("/shop-details/{idGiay}/{idMau}")
    private String getFormDetail(Model model,@PathVariable UUID idGiay, @PathVariable UUID idMau){

        KhachHang khachHang = (KhachHang) session.getAttribute("KhachHangLogin");

        Giay giay = giayService.getByIdGiay(idGiay);
        MauSac mau = mauSacService.getByIdMauSac(idMau);

        if (giay == null){
            giay = giayService.getByIdGiay(idMau);
            mau = mauSacService.getByIdMauSac(idGiay);
        }

        checkKHLogged(model, khachHang, giay, mau);

        List<ChiTietGiay> listCTGByGiay = giayChiTietService.findByMauSacAndGiay(mau, giay,1);
        List<ChiTietGiay> listCTGByGiaySold = giayChiTietService.findByMauSacAndGiay(mau, giay,0);

        List<Object[]> allSizeByGiay = new ArrayList<>();
        List<Object[]> allSizeByGiaySold = new ArrayList<>();

        String showReceiveMail = "true";

        for (ChiTietGiay x : listCTGByGiay) {
            if (x.getTrangThai()==1){
                showReceiveMail = "false";
            }
            allSizeByGiay.add(new Object[] { x.getSize().getSoSize(), x.getIdCTG(), showReceiveMail});
        }
        for (ChiTietGiay x : listCTGByGiaySold) {
            if (x.getTrangThai()==0){
                showReceiveMail = "true";
            }
            allSizeByGiaySold.add(new Object[] { x.getSize().getSoSize(), x.getIdCTG(), showReceiveMail});
        }
        allSizeByGiay.addAll(allSizeByGiaySold);

        allSizeByGiay.sort(Comparator.comparingInt(obj -> ((Integer) obj[0])));

        List<MauSac> listMauSacByGiay = giayChiTietService.findDistinctMauSacByGiay(giay);

        if (listMauSacByGiay.size() == 1){
            model.addAttribute("CTGBy1Mau", true);
            model.addAttribute("tenMau", mau.getTenMau());
        }else {
            model.addAttribute("CTGByMoreMau", true);
            model.addAttribute("listMauSacByGiay", listMauSacByGiay);
        }

        //Infor begin
        Optional<Double> maxPriceByGiay = listCTGByGiay.stream()
                .map(ChiTietGiay :: getGiaBan)
                .max(Double :: compare);

        Double maxPrice = maxPriceByGiay.get();

        int sumCTGByGiay = listCTGByGiay.stream()
                .mapToInt(ChiTietGiay::getSoLuong)
                .sum();

        Optional<Double> minPriceByGiay = listCTGByGiay.stream()
                .map(ChiTietGiay :: getGiaBan)
                .min(Double :: compare);

        Double minPrice = minPriceByGiay.get();

        //Infor end

        HinhAnh hinhAnhByGiayAndMau = giayChiTietService.hinhAnhByGiayAndMau(giay, mau);

        String maMau = mau.getMaMau();

        model.addAttribute("product", giay);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sunProductAvaible", sumCTGByGiay);
        model.addAttribute("hinhAnh", hinhAnhByGiayAndMau);
        model.addAttribute("idHeartMau", mau.getIdMau());
        model.addAttribute("listMauSacByGiay", listMauSacByGiay);
        model.addAttribute("listSizeCTG", allSizeByGiay);
        model.addAttribute("listGiavaSize", listCTGByGiay);

        model.addAttribute(maMau, "true");
        return "online/detail-product";
    }

    @GetMapping("/shop-details/sold/{idGiay}/{idMau}")
    private String getFormDetailSold(Model model,@PathVariable UUID idGiay, @PathVariable UUID idMau){

        KhachHang khachHang = (KhachHang) session.getAttribute("KhachHangLogin");

        Giay giay = giayService.getByIdGiay(idMau);
        MauSac mau = mauSacService.getByIdMauSac(idGiay);

        if (giay ==null){
            giay=giayService.getByIdGiay(idGiay);;
            mau = mauSacService.getByIdMauSac(idMau);
        }


        checkKHLogged(model, khachHang, giay, mau);

        model.addAttribute("buyReceiveMail", true);

        List<ChiTietGiay> listCTGByGiay = giayChiTietService.getCTGByGiaySoldOut(giay);

        List<MauSac> listMauSacByGiay = giayChiTietService.findDistinctMauSacByGiay(giay);

        if (listMauSacByGiay.size() == 1){
            model.addAttribute("CTGBy1Mau", true);
            model.addAttribute("tenMau", mau.getTenMau());
        }else {
            model.addAttribute("CTGByMoreMau", true);
            model.addAttribute("listMauSacByGiay", listMauSacByGiay);
        }


        Optional<Double> maxPriceByGiay = listCTGByGiay.stream()
                .map(ChiTietGiay :: getGiaBan)
                .max(Double :: compare);

        Double maxPrice = maxPriceByGiay.get();

        int sumCTGByGiay = listCTGByGiay.stream()
                .mapToInt(ChiTietGiay::getSoLuong)
                .sum();

        Optional<Double> minPriceByGiay = listCTGByGiay.stream()
                .map(ChiTietGiay :: getGiaBan)
                .min(Double :: compare);

        Double minPrice = minPriceByGiay.get();

        String material = giay.getChatLieu().getTenChatLieu();

        String brand = giay.getHang().getTenHang();

        HinhAnh hinhAnhByGiayAndMau = giayChiTietService.hinhAnhByGiayAndMau(giay, mau);

        model.addAttribute("hinhAnh", hinhAnhByGiayAndMau);
        model.addAttribute("material", material);
        model.addAttribute("nameBrand", brand);
        model.addAttribute("product", giay);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("listSizeCTG", listCTGByGiay);
        model.addAttribute("sunProductAvaible", sumCTGByGiay);
        model.addAttribute("listProducts", listCTGByGiay);
        model.addAttribute("listMauSacByGiay", listMauSacByGiay);

        return "online/detail-product";
    }

    @GetMapping("/shop/addProductCart")
    public String handleAddToCart(@RequestParam("idDetailProduct") UUID idDProduct, @RequestParam("quantity") int quantity, Model model) {

        ChiTietGiay ctg = giayChiTietService.getByIdChiTietGiay(idDProduct);

        GioHang gioHang = (GioHang) session.getAttribute("GHLogged") ;

        GioHangChiTiet gioHangChiTiet = ghctService.findByCTSPActive(ctg);

        if (gioHangChiTiet != null){
            gioHangChiTiet.setSoLuong(gioHangChiTiet.getSoLuong() + quantity);
            gioHangChiTiet.setTgThem(new Date());
            gioHangChiTiet.setDonGia(quantity*ctg.getGiaBan());
            gioHangChiTiet.setDonGiaTruocKhiGiam(quantity*ctg.getSoTienTruocKhiGiam());
            ghctService.addNewGHCT(gioHangChiTiet);
        }else {
            GioHangChiTiet gioHangChiTietNew = new GioHangChiTiet();

            gioHangChiTietNew.setChiTietGiay(ctg);
            gioHangChiTietNew.setGioHang(gioHang);
            gioHangChiTietNew.setSoLuong(quantity);
            gioHangChiTietNew.setTgThem(new Date());
            gioHangChiTietNew.setDonGia(quantity * ctg.getGiaBan());
            System.out.println(quantity*ctg.getSoTienTruocKhiGiam());
//            gioHangChiTiet.setDonGiaTruocKhiGiam(quantity*ctg.getSoTienTruocKhiGiam());
            gioHangChiTietNew.setTrangThai(1);

            ghctService.addNewGHCT(gioHangChiTietNew);
        }

        String idGiay = String.valueOf(ctg.getGiay().getIdGiay());

        String idMau = String.valueOf(ctg.getMauSac().getIdMau());

        String link = idGiay +"/" +idMau;
        return "redirect:/buyer/shop-details/" + link;
    }



    private void checkKHLogged(Model model, KhachHang khachHang, Giay giay, MauSac mauSac){
        if (khachHang != null){
            String fullName = khachHang.getHoTenKH();
            model.addAttribute("fullNameLogin", fullName);
            GioHang gioHang = (GioHang) session.getAttribute("GHLogged") ;

            List<GioHangChiTiet> listGHCTActive = ghctService.findByGHActive(gioHang);
            model.addAttribute("heartLogged", true);

            Integer sumProductInCart = listGHCTActive.size();
            model.addAttribute("sumProductInCart", sumProductInCart);
            model.addAttribute("buyNowAddCartLogged", true);

        }else {

            model.addAttribute("messageLoginOrSignin", true);
        }
    }

}
