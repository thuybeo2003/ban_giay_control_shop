package com.example.demo.service.impls;

import com.example.demo.model.*;
import com.example.demo.repository.HoaDonRepository;
import com.example.demo.service.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class HoaDonServiceImpl implements HoaDonService {

    @Autowired
    private HoaDonRepository hoaDonRepository;


    @Override
    public List<HoaDon> getListHoaDonChuaThanhToan() {
        return hoaDonRepository.listChuaThanhToan();
    }

    @Override
    public void add(HoaDon hoaDon) {
        hoaDonRepository.save(hoaDon);
    }

    @Override
    public HoaDon getOne(UUID id) {
        return hoaDonRepository.findById(id).orElse(null);
    }

    @Override
    public List<HoaDon> getAllHoaDon() {
        return hoaDonRepository.findAllByOrderByTgTaoDesc();
    }

    @Override
    public List<HoaDon> listAllHoaDonKhachHangOnline(KhachHang khachHang) {
        return hoaDonRepository.findByKhachHangAndLoaiHDAndTrangThaiOrTrangThaiOrTrangThaiOrTrangThaiOrTrangThaiOrTrangThaiOrderByTgTaoDesc(khachHang, 0, 0, 1, 2, 3, 4, 5);
    }

    @Override
    public List<HoaDon> listHoaDonKhachHangAndTrangThaiOnline(KhachHang khachHang, int trangThai) {
        return hoaDonRepository.findByKhachHangAndLoaiHDAndTrangThaiOrderByTgTaoDesc(khachHang, 0 , trangThai);
    }

    @Override
    public List<HoaDon> listHoaDonKhachHangAndTrangThaiOnlineAndLoaiThanhToan(KhachHang khachHang, int trangThai, int loaiThanhToan) {
        return hoaDonRepository.findByKhachHangAndLoaiHDAndTrangThaiAndHinhThucThanhToanOrderByTgTaoDesc(khachHang, 0, trangThai, loaiThanhToan);
    }

    @Override
    public List<HoaDon> findHoaDonByKhachHang(KhachHang khachHang) {
        return hoaDonRepository.findByKhachHangAndLoaiHDOrderByTgTaoDesc(khachHang, 0);
    }


    @Override
    public List<HoaDon> getAllHoaDonOffLine() {
        return hoaDonRepository.findHoaDonByLoaiHDOrderByTgTaoDesc(1);
    }

    @Override
    public List<HoaDon> listHoaDonOnline() {
        return hoaDonRepository.findByLoaiHDOrderByTgTaoDesc(0);
    }

    @Override
    public List<HoaDon> listAllHoaDonOnline() {
        return hoaDonRepository.findByLoaiHDAndTrangThaiOrTrangThaiOrTrangThaiOrTrangThaiOrTrangThaiOrTrangThaiOrderByTgTaoDesc(0, 0, 1 ,2 ,3 ,4 ,5 );
    }

    @Override
    public List<HoaDon> listHoaDonOnlineAndTrangThai(int trangThai) {
        return hoaDonRepository.findByLoaiHDAndTrangThaiOrderByTgTaoDesc(0, trangThai);
    }

    @Override
    public List<HoaDon> listHoaDonOnlineGiaoHang(int trangThai1, int trangThai2) {
        return hoaDonRepository.findByLoaiHDAndTrangThaiOrTrangThaiOrderByTgTaoDesc(0, 1, 2);
    }

    @Override
    public List<HoaDon> listHoaDonOnlineAndHTTT(int httt) {
        return hoaDonRepository.findByLoaiHDAndHinhThucThanhToanOrderByTgTaoDesc(0, httt);
    }

    @Override
    public List<HoaDon> listHoaDonByNhanVienAndTrangThai(NhanVien nhanVien, int trangThai) {
        return hoaDonRepository.findByNhanVienAndLoaiHDAndTrangThaiOrderByTgTaoDesc(nhanVien, 0, trangThai);
    }

    @Override
    public List<HoaDon> listHoaDonHuyAndThanhCongByNhanVien(NhanVien nhanVien) {
        return hoaDonRepository.findByNhanVienAndLoaiHDAndTrangThaiOrTrangThaiOrderByTgTaoDesc(nhanVien,  0 , 3,4);
    }

    @Override
    public List<HoaDon> listAllHoaDonByNhanVien(NhanVien nhanVien) {
        return hoaDonRepository.findByNhanVienOrderByTgTaoDesc(nhanVien);
    }

    @Override
    public List<HoaDon> listAllHoaDonByNhanVienHienTai(NhanVien nhanVien) {
        return hoaDonRepository.listAllHoaDonByNhanVienHienTai();
    }

    @Override
    public List<HoaDon> listHoaDonOnlineAndHTTTAndTrangThai(int httt, int trangThai) {
        return hoaDonRepository.findByLoaiHDAndTrangThaiAndHinhThucThanhToanOrderByTgTaoDesc(0, trangThai, httt);
    }



}
