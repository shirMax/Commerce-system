package util.Enums;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public enum PermissionType {
    ADD_OR_REMOVE_STORE_OWNER(1<<0),
    MANAGE_STORE_MANAGER(1<<1),//manager all managers workers
    MAKE_STORE_INACTIVE(1<<2),
    STORAGE_MANAGEMENT(1<<3),
    CHANGE_STORE_POLICY(1<<4),
    CHANGE_OWNER_PERMISSIONS(1<<5),
    GET_EMPLOYEES_DATA(1<<6),
    GET_PURCHASE_HISTORY(1<<7),
    REPLY_TO_MESSAGES(1<<8),
    STORE_MANAGEMENT(1<<9), //for general management like update store info
    MANAGE_OFFERS(1<<11),
    MANAGE_CONTRACTS(1<<12);

    private final int numVal;
    PermissionType(int numVal) {
        this.numVal = numVal;
    }

    public int getNumVal(){
        return numVal;
    }

    public static int collectionToBitmap(Collection<PermissionType> perms){
        int bitmap = 0;
        for (PermissionType perm : perms)
            bitmap = bitmap | perm.getNumVal();
        return bitmap;
    }

    public static Set<PermissionType> bitmapToSet(int bitmap){
        Set<PermissionType> set = new HashSet<>();
        for (PermissionType perm : PermissionType.values())
            if ((bitmap & perm.getNumVal()) != 0)
                set.add(perm);
        return set;
    }
}
