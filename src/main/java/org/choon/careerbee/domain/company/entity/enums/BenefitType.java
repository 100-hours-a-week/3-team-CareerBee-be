package org.choon.careerbee.domain.company.entity.enums;

public enum BenefitType {
  COMPENSATION("보상/수당"),
  LEAVE("휴가/휴직"),
  TRANSPORT_MEALS("교통/식사"),
  EDUCATION_EVENTS("교육/행사"),
  WELLNESS("생활/건강"),
  ETC("기타");

  private final String label;

  BenefitType(String label) {
    this.label = label;
  }

  public static String fromLabel(String label) {
    return switch (label) {
      case "보상/수당" -> COMPENSATION.name();
      case "휴가/휴직" -> LEAVE.name();
      case "교통/식사" -> TRANSPORT_MEALS.name();
      case "교육/행사" -> EDUCATION_EVENTS.name();
      case "생활/건강" -> WELLNESS.name();
      default -> ETC.name();
    };
  }
}
