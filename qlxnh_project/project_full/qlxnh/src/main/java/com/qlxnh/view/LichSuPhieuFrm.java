public void loadTable() {
    try {
        LichSuPhieuDAO dao = new LichSuPhieuDAO();
        List<LichSuPhieu> list = dao.getAll();

        DefaultTableModel model = (DefaultTableModel) tbl.getModel();
        model.setRowCount(0);

        for (LichSuPhieu p : list) {
            model.addRow(new Object[]{
                p.getMaPhieu(),
                p.getLoai(),
                p.getNgay(),
                p.getNguoiLap(),
                p.getSoDong(),
                p.getTongTien()
            });
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}