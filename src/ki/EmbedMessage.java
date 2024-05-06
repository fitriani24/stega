package ki;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.util.Base64;
import java.util.Random;


public class EmbedMessage extends JFrame implements ActionListener {

    JButton open = new JButton("Cari"), embed = new JButton("Sisipkan"),
            save = new JButton("Simpan di"), reset = new JButton("Exit"), enkrip = new JButton("Enkripsi");
    JTextArea message = new JTextArea();

    JTextArea encryptedMessage = new JTextArea(); // Kolom untuk hasil enkripsi
    JTextField secretKeyField = new JTextField(8); // Input Secret Key
    BufferedImage sourceImage = null, embeddedImage = null;
    JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    JScrollPane originalPane = new JScrollPane(), embeddedPane = new JScrollPane();

    
    public EmbedMessage() {
        super("Form Penyisipan Pesan Kombinasi DES dengan RPE");
        assembleInterface();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setSize(500, 500);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setVisible(true);
        sp.setDividerLocation(0.5);
        this.validate();
    }

    private void assembleInterface() {
        JPanel southPanel = new JPanel(new BorderLayout());

// Create a panel for the secret key components
        JPanel secretKeyPanel = new JPanel(new FlowLayout());
        secretKeyPanel.add(new JLabel("Secret Key:")); // Label untuk secret key decrypt
        secretKeyPanel.add(secretKeyField); // Field untuk input secret key

        // Add the secret key panel to the north position of southPanel
        southPanel.add(secretKeyPanel, BorderLayout.NORTH);

        // Create a panel for the buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(enkrip);
        buttonPanel.add(open);
        buttonPanel.add(embed);
        buttonPanel.add(save);
        buttonPanel.add(reset);

        // Add the button panel to the center position of southPanel
        southPanel.add(buttonPanel, BorderLayout.CENTER);

        this.getContentPane().add(southPanel, BorderLayout.SOUTH);
        open.addActionListener(this);
        embed.addActionListener(this);
        save.addActionListener(this);
        reset.addActionListener(this);
        enkrip.addActionListener(this);
        open.setMnemonic('O');
        embed.setMnemonic('E');
        save.setMnemonic('S');
        reset.setMnemonic('R');
        enkrip.setMnemonic('E');

        JPanel northPanel = new JPanel(new GridLayout(2, 2));
        northPanel.setBorder(BorderFactory.createTitledBorder("Pesan Rahasia dan Hasil Enkripsi DES"));

        message.setFont(new Font("Dialog", Font.BOLD, 20));
        northPanel.add(new JScrollPane(message));
        
        message.setPreferredSize(new Dimension(300, 150)); // Mengatur ukuran JTextArea menjadi sedang
        encryptedMessage.setFont(new Font("Dialog", Font.BOLD, 20));
        encryptedMessage.setEditable(false);
        northPanel.add(new JScrollPane(encryptedMessage));


        this.getContentPane().add(northPanel, BorderLayout.NORTH);

        sp.setLeftComponent(originalPane);
        sp.setRightComponent(embeddedPane);
        originalPane.setBorder(BorderFactory.createTitledBorder("Gambar Asli"));
        embeddedPane.setBorder(BorderFactory.createTitledBorder("Citra Stego"));
        this.getContentPane().add(sp, BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent ae) {
        Object o = ae.getSource();
        if (o == open)
            openImage();
        else if (o == embed)
            embedMessage();
        else if (o == save)
            saveImage();
        else if (o == reset)
            dispose();
        else if (o == enkrip) {
            if (secretKeyField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Masukkan secret key terlebih dahulu.", "Secret Key Kosong", JOptionPane.ERROR_MESSAGE);
            } else {
                String secretKey = secretKeyField.getText();
                if (secretKey.length() != 8) {
                    JOptionPane.showMessageDialog(this, "Secret key harus terdiri dari 8 karakter.", "Panjang Secret Key Salah", JOptionPane.ERROR_MESSAGE);
                } else {
                    enkripDES();
                }
            }
        }
    }

    private java.io.File showFileDialog(final boolean open) {
        JFileChooser fc = new JFileChooser("Open an image");
        javax.swing.filechooser.FileFilter ff = new javax.swing.filechooser.FileFilter() {
            public boolean accept(java.io.File f) {
                String name = f.getName().toLowerCase();
                if (open)
                    return f.isDirectory() || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif") || name.endsWith(".tiff") || name.endsWith(".bmp") || name.endsWith(".dib");
                return f.isDirectory() || name.endsWith(".png") || name.endsWith(".bmp");
            }

            public String getDescription() {
                if (open)
                    return "Image (*.jpg, *.jpeg, *.png, *.gif, *.tiff, *.bmp, *.dib)";
                return "Image (*.png, *.bmp)";
            }
        };
        fc.setAcceptAllFileFilterUsed(false);
        fc.addChoosableFileFilter(ff);

        java.io.File f = null;
        if (open && fc.showOpenDialog(this) == fc.APPROVE_OPTION)
            f = fc.getSelectedFile();
        else if (!open && fc.showSaveDialog(this) == fc.APPROVE_OPTION)
            f = fc.getSelectedFile();
        return f;
    }

    private void openImage() {
        java.io.File f = showFileDialog(true);
        try {
            sourceImage = ImageIO.read(f);
            JLabel l = new JLabel(new ImageIcon(sourceImage));
            originalPane.getViewport().add(l);
            this.validate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void embedMessage() {
        String mess = encryptedMessage.getText();
        embeddedImage = sourceImage.getSubimage(0, 0, sourceImage.getWidth(), sourceImage.getHeight());
        embedMessage(embeddedImage, mess);
        JLabel l = new JLabel(new ImageIcon(embeddedImage));
        embeddedPane.getViewport().add(l);
        this.validate();
    }

    private void embedMessage(BufferedImage img, String mess) {
        int messageLength = mess.length();
        int imageWidth = img.getWidth(), imageHeight = img.getHeight(), imageSize = imageWidth * imageHeight;

        if (messageLength * 8 + 32 > imageSize) {
            JOptionPane.showMessageDialog(this, "Pesan terlalu panjang untuk disisipkan dalam gambar.", "Kesalahan", JOptionPane.ERROR_MESSAGE);
            return;
        }
        embedInteger(img, messageLength, 1, 0);

        byte b[] = mess.getBytes();
        for (int i = 0; i < b.length; i++) {
            embedByte(img, b[i], i * 8 + 32, 0);
        }

        embedInteger(img, 0, b.length * 8 + 32, 0);
        JOptionPane.showMessageDialog(this, "Pesan disisipkan dalam gambar dengan sukses!", "Proses Berhasil", JOptionPane.INFORMATION_MESSAGE);
    }

    private void embedInteger(BufferedImage img, int n, int start, int storageBit) {
        int maxX = img.getWidth();
        int maxY = img.getHeight();
        Random random = new Random(start);
        int count = 0;

        while (count < 32) {
            int x = random.nextInt(maxX);
            int y = random.nextInt(maxY);

            int rgb = img.getRGB(x, y);
            int bit = getBitValue(n, count);
            rgb = setBitValue(rgb, storageBit, bit);
            System.out.println("Embed value: " + x + "," + y);

            img.setRGB(x, y, rgb);

            count++;
        }
    }

    private void embedByte(BufferedImage img, byte b, int start, int storageBit) {
        int maxX = img.getWidth();
        int maxY = img.getHeight();
        Random random = new Random(start);
        int count = 0;

        while (count < 8) {
            int x = random.nextInt(maxX);
            int y = random.nextInt(maxY);

            int rgb = img.getRGB(x, y);
            int bit = getBitValue(b, count);
            rgb = setBitValue(rgb, storageBit, bit);
            img.setRGB(x, y, rgb);

            count++;
        }
    }



    private int getBitValue(int n, int location) {
        int v = n & (int) Math.round(Math.pow(2, location));
        return v == 0 ? 0 : 1;
    }

    private int setBitValue(int n, int location, int bit) {
        int toggle = (int) Math.pow(2, location), bv = getBitValue(n, location);
        if (bv == bit)
            return n;
        if (bv == 0 && bit == 1)
            n |= toggle;
        else if (bv == 1 && bit == 0)
            n ^= toggle;
        return n;
    }

    private void saveImage() {
        if (embeddedImage == null) {
            JOptionPane.showMessageDialog(this, "Belum ada pesan yang disisipkan!", "Kesalahan", JOptionPane.ERROR_MESSAGE);
            return;
        }
        java.io.File f = showFileDialog(false);
        String name = f.getName();
        String ext = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
        if (!ext.equals("png") && !ext.equals("bmp")) {
            ext = "png";
            f = new java.io.File(f.getAbsolutePath() + ".png");
        }
        try {
            if (f.exists()) {
                f.delete();
            }
            ImageIO.write(embeddedImage, ext.toUpperCase(), f);
            JOptionPane.showMessageDialog(this, "Gambar berhasil disimpan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void resetInterface() {
        message.setText("");
        encryptedMessage.setText("");
        secretKeyField.setText("");
        originalPane.getViewport().removeAll();
        embeddedPane.getViewport().removeAll();
        sourceImage = null;
        embeddedImage = null;
        sp.setDividerLocation(0.5);
        this.validate();
    }

    private void enkripDES() {
        String secretKey = secretKeyField.getText();
        String pesan = message.getText();

        try {
            // Konversi secret key menjadi objek SecretKey
            byte[] keyBytes = secretKey.getBytes();
            SecretKey key = new SecretKeySpec(keyBytes, "DES");

            // Buat objek Cipher dengan mode ECB
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            // Enkripsi pesan
            byte[] encryptedBytes = cipher.doFinal(pesan.getBytes());

            // Konversi hasil enkripsi ke dalam format Base64
            String encryptedText = Base64.getEncoder().encodeToString(encryptedBytes);

            // Tampilkan hasil enkripsi pada JTextArea
            encryptedMessage.setText(encryptedText);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String arg[]) {
        new EmbedMessage();
    }
}
