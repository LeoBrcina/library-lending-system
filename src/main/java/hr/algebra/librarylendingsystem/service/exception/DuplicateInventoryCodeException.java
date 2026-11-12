package hr.algebra.librarylendingsystem.service.exception;

public class DuplicateInventoryCodeException
        extends InvalidBookCopyException {

    public DuplicateInventoryCodeException(String inventoryCode) {
        super("Inventory code already exists: " + inventoryCode);
    }
}
