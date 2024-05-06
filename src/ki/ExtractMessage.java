package ki;

import java.awt.image.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.imageio.*;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;
import javax.crypto.KeyGenerator;

public class ExtractMessage extends JFrame implements ActionListener {
    JButton open = new JButton("Cari"), decode = new JButton("Ekstrak"),
            reset = new JButton("Exit"), decrypt = new JButton("Deksripsi");
    JTextArea message = new JTextArea();
    JTextArea decryptedMessage = new JTextArea();
    JTextField secretKeyField = new JTextField(8); // Field untuk secret key decrypt

    BufferedImage image = null;
    JScrollPane imagePane = new JScrollPane();

    public ExtractMessage() {
        super("Form Ekstraksi Pesan Modifikasi LSB dengan RPE");
        assembleInterface();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setSize(500, 500);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setVisible(true);
    }

    private void assembleInterface() {
        JPanel p = new JPanel(new GridLayout(2, 1));
        // Panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(open);
        buttonPanel.add(decode);
        buttonPanel.add(decrypt);
        buttonPanel.add(reset);

        // Panel for secret key
        JPanel secretKeyPanel = new JPanel(new FlowLayout());
        secretKeyPanel.add(new JLabel("Secret Key:")); // Label untuk secret key decrypt
        secretKeyPanel.add(secretKeyField); // Field untuk input secret key

        p.add(buttonPanel);
        p.add(secretKeyPanel);

        
        this.getContentPane().add(p, BorderLayout.NORTH);
        open.addActionListener(this);
        decode.addActionListener(this);
        reset.addActionListener(this);
        decrypt.addActionListener(this);
        open.setMnemonic('O');
        decode.setMnemonic('D');
        reset.setMnemonic('R');
        decrypt.setMnemonic('C');

        p = new JPanel(new GridLayout(2, 1));
        p.add(new JScrollPane(message));
        p.add(new JScrollPane(decryptedMessage));
        message.setFont(new Font("Imprint MT Shadow", Font.BOLD, 20));
        message.setPreferredSize(new Dimension(300, 150)); // Mengatur ukuran JTextArea menjadi sedang
        decryptedMessage.setFont(new Font("Imprint MT Shadow", Font.BOLD, 20));
        p.setBorder(BorderFactory.createTitledBorder("Pesan rahasia dan Hasil Dekripsi"));
        message.setEditable(false);
        decryptedMessage.setEditable(false);
        this.getContentPane().add(p, BorderLayout.SOUTH);
                
        imagePane.setBorder(BorderFactory.createTitledBorder("Citra stego"));
        this.getContentPane().add(imagePane, BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent ae) {
        Object o = ae.getSource();
        if (o == open)
            openImage();
        else if (o == decode)
            decodeMessage();
        else if (o == reset)
            dispose();
        else if (o == decrypt)
            decryptDES();
    }

    private java.io.File showFileDialog(boolean open) {
        JFileChooser fc = new JFileChooser("Open an image");
        javax.swing.filechooser.FileFilter ff = new javax.swing.filechooser.FileFilter() {
            public boolean accept(java.io.File f) {
                String name = f.getName().toLowerCase();
                return f.isDirectory() || name.endsWith(".png");
            }

            public String getDescription() {
                return "Image (*.png)";
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
            image = ImageIO.read(f);
            JLabel l = new JLabel(new ImageIcon(image));
            imagePane.getViewport().add(l);
            this.validate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void decodeMessage() {
        int messageLength = extractInteger(image, 1, 0); // Extract the message length from the image

        if (messageLength <= 0) {
            JOptionPane.showMessageDialog(null, "Pesan tidak ditemukan dalam gambar.", "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }

        byte[] messageBytes = new byte[messageLength];
        for (int i = 0; i < messageLength; i++) {
            byte extractedByte = (byte) extractByte(image, i * 8 + 32, 0); // Extract each byte of the message
            messageBytes[i] = extractedByte;
        }

        message.setText(new String(messageBytes));

    }

    private static int extractInteger(BufferedImage img, int start, int storageBit) {
        int maxX = img.getWidth();
        int maxY = img.getHeight();
        Random random = new Random(start);
        int extractedValue = 0;
        int count = 0;

        while (count < 32) {
            int x = random.nextInt(maxX), y = random.nextInt(maxY), rgb = img.getRGB(x, y), bit = getBitValue(rgb, storageBit);
            extractedValue = setBitValue(extractedValue, count, bit);
            System.out.println("Extract value: " + x + "," + y);


            count++;
        }

        return extractedValue;
    }

    private static byte extractByte(BufferedImage img, int start, int storageBit) {
        int maxX = img.getWidth();
        int maxY = img.getHeight();
        Random random = new Random(start);
        byte extractedByte = 0;
        int count = 0;

        while (count < 8) {
            int x = random.nextInt(maxX);
            int y = random.nextInt(maxY);

            int rgb = img.getRGB(x, y);
            int bit = getBitValue(rgb, storageBit);
            extractedByte = (byte) setBitValue(extractedByte, count, bit);

            count++;
        }

        return extractedByte;
    }

    private static int getBitValue(int n, int location) {
        int v = n & (int) Math.round(Math.pow(2, location));
        return v == 0 ? 0 : 1;
    }

    private static int setBitValue(int n, int location, int bit) {
        int toggle = (int) Math.pow(2, location);
        int bv = getBitValue(n, location);
        if (bv == bit) {
            return n;
        }
        if (bv == 0 && bit == 1) {
            n |= toggle;
        } else if (bv == 1 && bit == 0) {
            n ^= toggle;
        }
        return n;
    }



    private void resetInterface() {
        message.setText("");
        decryptedMessage.setText("");
        imagePane.getViewport().removeAll();
        image = null;
        this.validate();
    }
    private void decryptDES() {
    String encryptedText = message.getText();
    String secretKey = secretKeyField.getText();

    if (secretKey.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Masukkan secret key terlebih dahulu.", "Secret Key Kosong", JOptionPane.ERROR_MESSAGE);
    } else {
        if (secretKey.length() != 8) {
            JOptionPane.showMessageDialog(this, "Secret key harus terdiri dari 8 karakter.", "Panjang Secret Key Salah", JOptionPane.ERROR_MESSAGE);
        } else {
            String decryptedMessage = decryptDES(encryptedText, secretKey);
            if (decryptedMessage == null) {
                JOptionPane.showMessageDialog(this, "Secret key salah. Dekripsi gagal.", "Secret Key Salah", JOptionPane.ERROR_MESSAGE);
            } else {
                decryptedMessage = decryptedMessage.trim();
                this.decryptedMessage.setText(decryptedMessage);
            }
        }
    }
}


    private String decryptDES(String encryptedText, String secretKey) {
        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
            DESKeySpec desKeySpec = new DESKeySpec(keyBytes);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secKey = keyFactory.generateSecret(desKeySpec);
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");

            cipher.init(Cipher.DECRYPT_MODE, secKey);

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return bytes;
    }

    public static void main(String arg[]) {
        new ExtractMessage();
    }
}
