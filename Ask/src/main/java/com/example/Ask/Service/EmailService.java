package com.example.Ask.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import jakarta.mail.MessagingException;
import java.io.InputStream;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private FileStorageService fileStorageService;

    @Value("${app.frontend.url:http://localhost:8083}")
    private String frontendUrl;

    public void send(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@petsystem.local");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            // Log the error but don't throw it to avoid breaking the registration
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }

    public void sendVerificationEmail(String to, String username, String verificationToken) {
        try {
            String subject = "Επιβεβαίωση Email - Pet Adoption System";
            String verificationUrl = frontendUrl + "/verify-email?token=" + verificationToken;
            
            String text = String.format(
                "Γεια σας %s,\n\n" +
                "Καλώς ήρθατε στο Pet Adoption System!\n\n" +
                "Για να ολοκληρώσετε την εγγραφή σας, παρακαλώ κάντε κλικ στον παρακάτω σύνδεσμο:\n\n" +
                "%s\n\n" +
                "Αυτός ο σύνδεσμος ισχύει για 24 ώρες.\n\n" +
                "Εάν δεν δημιουργήσατε εσείς αυτόν τον λογαριασμό, παρακαλώ αγνοήστε αυτό το email.\n\n" +
                "Με εκτίμηση,\n" +
                "Η ομάδα του Pet Adoption System",
                username, verificationUrl
            );
            
            send(to, subject, text);
        } catch (Exception e) {
            System.err.println("Failed to send verification email: " + e.getMessage());
        }
    }

    public void sendLoginNotification(String to, String username, String loginTime, String ipAddress) {
        try {
            String subject = "Ειδοποίηση Σύνδεσης - Pet Adoption System";
            
            String text = String.format(
                "Γεια σας %s,\n\n" +
                "Εντοπίστηκε μια νέα σύνδεση στον λογαριασμό σας:\n\n" +
                "Ώρα σύνδεσης: %s\n" +
                "IP Address: %s\n\n" +
                "Εάν δεν ήσασταν εσείς, παρακαλώ επικοινωνήστε αμέσως μαζί μας.\n\n" +
                "Με εκτίμηση,\n" +
                "Η ομάδα του Pet Adoption System",
                username, loginTime, ipAddress
            );
            
            send(to, subject, text);
        } catch (Exception e) {
            System.err.println("Failed to send login notification: " + e.getMessage());
        }
    }

    public void sendWelcomeEmail(String to, String username) {
        try {
            String subject = "Καλώς ήρθατε στο Pet Adoption System!";
            
            String text = String.format(
                "Γεια σας %s,\n\n" +
                "Καλώς ήρθατε στο Pet Adoption System!\n\n" +
                "Ο λογαριασμός σας έχει επιβεβαιωθεί επιτυχώς και μπορείτε τώρα να:\n" +
                "- Προβάλετε τα διαθέσιμα ζώα\n" +
                "- Κάνετε αιτήσεις υιοθεσίας\n" +
                "- Διαχειριστείτε το προφίλ σας\n\n" +
                "Ευχαριστούμε που επιλέξατε το σύστημά μας!\n\n" +
                "Με εκτίμηση,\n" +
                "Η ομάδα του Pet Adoption System",
                username
            );
            
            send(to, subject, text);
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
    }

    public void sendRequestSubmittedEmail(String to, String username, String animalName) {
        try {
            String subject = "Η αίτηση σας υποβλήθηκε - Pet Adoption System";
            
            String text = String.format(
                "Γεια σας %s,\n\n" +
                "Η αίτηση υιοθεσίας σας για το ζώο '%s' υποβλήθηκε επιτυχώς!\n\n" +
                "Η αίτησή σας βρίσκεται σε επεξεργασία και θα ενημερωθείτε σύντομα για την κατάστασή της.\n\n" +
                "Θα λάβετε email όταν:\n" +
                "- Η αίτηση εγκριθεί\n" +
                "- Χρειαστεί επιπλέον πληροφόρηση\n" +
                "- Η αίτηση ολοκληρωθεί\n\n" +
                "Ευχαριστούμε για το ενδιαφέρον σας!\n\n" +
                "Με εκτίμηση,\n" +
                "Η ομάδα του Pet Adoption System",
                username, animalName
            );
            
            send(to, subject, text);
        } catch (Exception e) {
            System.err.println("Failed to send request submitted email: " + e.getMessage());
        }
    }

    public void sendRequestApprovedEmail(String to, String username, String animalName) {
        try {
            String subject = "Η αίτηση σας εγκρίθηκε! - Pet Adoption System";
            
            String text = String.format(
                "Γεια σας %s,\n\n" +
                "Συγχαρητήρια! Η αίτηση υιοθεσίας σας για το ζώο '%s' εγκρίθηκε!\n\n" +
                "Το ζώο έχει προστεθεί στο σύστημα και είναι διαθέσιμο για υιοθεσία.\n\n" +
                "Μπορείτε να δείτε το ζώο στην λίστα των διαθέσιμων ζώων.\n\n" +
                "Ευχαριστούμε που επιλέξατε το σύστημά μας!\n\n" +
                "Με εκτίμηση,\n" +
                "Η ομάδα του Pet Adoption System",
                username, animalName
            );
            
            send(to, subject, text);
        } catch (Exception e) {
            System.err.println("Failed to send request approved email: " + e.getMessage());
        }
    }

    public void sendRequestRejectedEmail(String to, String username, String animalName) {
        try {
            String subject = "Ενημέρωση για την αίτηση σας - Pet Adoption System";
            
            String text = String.format(
                "Γεια σας %s,\n\n" +
                "Λυπούμαστε να σας ενημερώσουμε ότι η αίτηση υιοθεσίας σας για το ζώο '%s' δεν μπορεί να προχωρήσει.\n\n" +
                "Η αίτηση έχει αφαιρεθεί από το σύστημα.\n\n" +
                "Μπορείτε να υποβάλετε νέα αίτηση για άλλο ζώο αν θέλετε.\n\n" +
                "Ευχαριστούμε για το ενδιαφέρον σας.\n\n" +
                "Με εκτίμηση,\n" +
                "Η ομάδα του Pet Adoption System",
                username, animalName
            );
            
            send(to, subject, text);
        } catch (Exception e) {
            System.err.println("Failed to send request rejected email: " + e.getMessage());
        }
    }

    public void sendNewAnimalNotification(String to, String username, String animalName, String animalType, int animalAge, String animalGender) {
        sendNewAnimalNotification(to, username, animalName, animalType, animalAge, animalGender, null);
    }

    public void sendNewAnimalNotification(String to, String username, String animalName, String animalType, int animalAge, String animalGender, String imageUrl) {
        try {
            String subject = "Νέο ζώο διαθέσιμο για υιοθεσία! - Pet Adoption System";
            
            // Translate gender to Greek
            String genderText = animalGender;
            if ("Male".equalsIgnoreCase(animalGender)) {
                genderText = "Αρσενικό";
            } else if ("Female".equalsIgnoreCase(animalGender)) {
                genderText = "Θηλυκό";
            }
            
            // Check if we have an image to include
            boolean hasImage = imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("http");
            
            if (hasImage && fileStorageService.imageExists(imageUrl)) {
                // Send HTML email with embedded image
                try {
                    MimeMessage message = mailSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                    
                    helper.setFrom("noreply@petsystem.local");
                    helper.setTo(to);
                    helper.setSubject(subject);
                    
                    // Create HTML content
                    String htmlContent = String.format(
                        "<html><body style='font-family: Arial, sans-serif;'>" +
                        "<h2>Γεια σας %s,</h2>" +
                        "<p>Έχουμε νέα για εσάς! Ένα νέο ζώο έχει προστεθεί στο σύστημά μας:</p>" +
                        "<table style='border-collapse: collapse; margin: 20px 0;'>" +
                        "<tr><td style='padding: 5px; font-weight: bold;'>Όνομα:</td><td style='padding: 5px;'>%s</td></tr>" +
                        "<tr><td style='padding: 5px; font-weight: bold;'>Είδος:</td><td style='padding: 5px;'>%s</td></tr>" +
                        "<tr><td style='padding: 5px; font-weight: bold;'>Ηλικία:</td><td style='padding: 5px;'>%d έτη</td></tr>" +
                        "<tr><td style='padding: 5px; font-weight: bold;'>Φύλο:</td><td style='padding: 5px;'>%s</td></tr>" +
                        "</table>" +
                        "<div style='margin: 20px 0;'>" +
                        "<img src='cid:animalImage' style='max-width: 400px; max-height: 400px; border-radius: 8px;' alt='Εικόνα ζώου' />" +
                        "</div>" +
                        "<p>Αν ενδιαφέρεστε, μπορείτε να δείτε περισσότερες πληροφορίες και να υποβάλετε αίτηση υιοθεσίας.</p>" +
                        "<p>Ευχαριστούμε!</p>" +
                        "<p>Με εκτίμηση,<br/>Η ομάδα του Pet Adoption System</p>" +
                        "</body></html>",
                        username, animalName, animalType, animalAge, genderText
                    );
                    
                    helper.setText(htmlContent, true);
                    
                    // Get image from MinIO and attach it as inline
                    try {
                        InputStream imageStream = fileStorageService.getImageInputStream(imageUrl);
                        String contentType = "image/jpeg";
                        if (imageUrl.toLowerCase().endsWith(".png")) {
                            contentType = "image/png";
                        } else if (imageUrl.toLowerCase().endsWith(".gif")) {
                            contentType = "image/gif";
                        } else if (imageUrl.toLowerCase().endsWith(".webp")) {
                            contentType = "image/webp";
                        }
                        InputStreamResource imageResource = new InputStreamResource(imageStream);
                        helper.addInline("animalImage", imageResource, contentType);
                    } catch (Exception imgEx) {
                        System.err.println("Failed to attach image to email: " + imgEx.getMessage());
                        // Continue without image if attachment fails
                    }
                    
                    mailSender.send(message);
                    System.out.println("HTML email with image sent successfully to " + to);
                } catch (MessagingException e) {
                    System.err.println("Failed to send HTML email, falling back to plain text: " + e.getMessage());
                    // Fallback to plain text
                    sendPlainTextNotification(to, username, animalName, animalType, animalAge, genderText, subject);
                }
            } else {
                // Send plain text email (no image or image not available)
                sendPlainTextNotification(to, username, animalName, animalType, animalAge, genderText, subject);
            }
        } catch (Exception e) {
            System.err.println("Failed to send new animal notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void sendPlainTextNotification(String to, String username, String animalName, String animalType, int animalAge, String genderText, String subject) {
            String text = String.format(
                "Γεια σας %s,\n\n" +
                "Έχουμε νέα για εσάς! Ένα νέο ζώο έχει προστεθεί στο σύστημά μας:\n\n" +
                "Όνομα: %s\n" +
                "Είδος: %s\n" +
                "Ηλικία: %d έτη\n" +
                "Φύλο: %s\n\n" +
                "Αν ενδιαφέρεστε, μπορείτε να δείτε περισσότερες πληροφορίες και να υποβάλετε αίτηση υιοθεσίας.\n\n" +
                "Ευχαριστούμε!\n\n" +
                "Με εκτίμηση,\n" +
                "Η ομάδα του Pet Adoption System",
                username, animalName, animalType, animalAge, genderText
            );
            send(to, subject, text);
    }
}
