package Module.enums;

public enum Sections {

    EMAIL_SETTINGS(0), NETWORK_SETTINGS(1), SCHEDULE_SETTINGS(2), DEBUG(3);

    private int section_number;

    Sections(int section_number) {
        this.section_number = section_number;
    }

    public int getSection_number() {
        return section_number;
    }
}
