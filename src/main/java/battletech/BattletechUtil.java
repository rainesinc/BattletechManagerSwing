/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package battletech;

/**
 *
 * @author ryan
 */
public class BattletechUtil {

    public static int movementHeat(String moveMode, int distance, boolean stoodUp, int standAttempts) {
        int heat = 0;
        if (moveMode != null && "walk".equalsIgnoreCase(moveMode)) {
            heat = heat + 1;
        } else if (moveMode != null && ("run".equalsIgnoreCase(moveMode) || "charge".equalsIgnoreCase(moveMode))) {
            heat = heat + 2;
        } else if (moveMode != null && ("jump".equalsIgnoreCase(moveMode) || "pounce".equalsIgnoreCase(moveMode))) {
            if (distance > 3) {
                heat = heat + distance;
            } else {
                heat = heat + 3;
            }
        }
        if (stoodUp) {
            heat = heat + standAttempts;
        }
        return heat;
    }

    public static int weaponHeat(String weapon) {
        int heat = 0;
        if (weapon != null && !"".equalsIgnoreCase(weapon)) {
            if ("f".equalsIgnoreCase(weapon)) {
                heat = 3;
            } else if ("ll".equalsIgnoreCase(weapon)) {
                heat = 8;
            } else if ("ml".equalsIgnoreCase(weapon)) {
                heat = 3;
            } else if ("sl".equalsIgnoreCase(weapon)) {
                heat = 1;
            } else if ("ppc".equalsIgnoreCase(weapon)) {
                heat = 10;
            } else if ("ac2".equalsIgnoreCase(weapon)) {
                heat = 1;
            } else if ("ac5".equalsIgnoreCase(weapon)) {
                heat = 1;
            } else if ("ac10".equalsIgnoreCase(weapon)) {
                heat = 3;
            } else if ("ac20".equalsIgnoreCase(weapon)) {
                heat = 7;
            } else if ("mg".equalsIgnoreCase(weapon)) {
                heat = 0;
            } else if ("lrm5".equalsIgnoreCase(weapon)) {
                heat = 2;
            } else if ("lrm10".equalsIgnoreCase(weapon)) {
                heat = 4;
            } else if ("lrm15".equalsIgnoreCase(weapon)) {
                heat = 5;
            } else if ("lrm20".equalsIgnoreCase(weapon)) {
                heat = 6;
            } else if ("srm2".equalsIgnoreCase(weapon)) {
                heat = 2;
            } else if ("srm4".equalsIgnoreCase(weapon)) {
                heat = 3;
            } else if ("srm6".equalsIgnoreCase(weapon)) {
                heat = 4;
            }
        }
        return heat;
    }

    public static boolean isBallistic(String weapon) {
        boolean isBallistic = false;
        if (weapon != null) {
            if ("mg".equalsIgnoreCase(weapon) ||
                    "ac2".equalsIgnoreCase(weapon) ||
                    "ac5".equalsIgnoreCase(weapon) ||
                    "ac10".equalsIgnoreCase(weapon) ||
                    "ac20".equalsIgnoreCase(weapon) ||
                    "lrm5".equalsIgnoreCase(weapon) ||
                    "lrm10".equalsIgnoreCase(weapon) ||
                    "lrm15".equalsIgnoreCase(weapon) ||
                    "lrm20".equalsIgnoreCase(weapon) ||
                    "srm2".equalsIgnoreCase(weapon) ||
                    "srm4".equalsIgnoreCase(weapon) ||
                    "srm6".equalsIgnoreCase(weapon)) {
                isBallistic = true;
            }
        }
        return isBallistic;
    }

    /**
     * From the attacker's perspective, affected by:
     * -Move mode
     * -Position (prone/upright)
     * -Heat mod
     * -Damage mod
     * @return
     */
    public static int attackerMod(String moveMode, String position,
            int heatMod, int sensorHits) {
        int mod = 0;
        // Move Mode.
        if (moveMode != null && "walk".equalsIgnoreCase(moveMode)) {
            mod = mod + 1;
        } else if (moveMode != null && ("run".equalsIgnoreCase(moveMode) || "charge".equalsIgnoreCase(moveMode))) {
            mod = mod + 2;
        } else if (moveMode != null && ("jump".equalsIgnoreCase(moveMode) || "pounce".equalsIgnoreCase(moveMode))) {
            mod = mod + 3;
        }
        // Position.
        if (position != null && "prone".equalsIgnoreCase(position)) {
            mod = mod + 2;
        }
        // HeatMod.
        mod = mod + heatMod;
        // Sensor Hits
        if (sensorHits == 1) {
            mod = mod + 2;
        } else if (sensorHits > 1) {
            mod = mod + 100;
        }
        return mod;
    }

    /**
     * From the TARGET's perspecitve
     * -secondary
     * -partial cover
     * -position (+ range)
     * -moveDistance (+ moveMode)
     * @return
     */
    public static int targetMod(boolean secondary, boolean partialCover, String position,
            String moveMode, int moveDistance, int range) {
        int mod = 0;
        // Secondary Target
        if (secondary) {
            mod = mod + 1;
        }
        // Partial Cover.
        if (partialCover) {
            mod = mod + 3;
        }
        // Position
        if (position != null) {
            if ("Prone".equalsIgnoreCase(position)) {
                if (range == 1) {
                    mod = mod - 2;
                } else {
                    mod = mod + 1;
                }
            }
        }
        // Movement
        switch (moveDistance) {
            case 0:
            case 1:
            case 2:
                break;
            case 3:
            case 4:
                mod = mod + 1;
                break;
            case 5:
            case 6:
                mod = mod + 2;
                break;
            case 7:
            case 8:
            case 9:
                mod = mod + 3;
                break;
            default:
                mod = mod + 4;
        }
        if (moveMode != null) {
            if ("jump".equalsIgnoreCase(moveMode) || "pounce".equalsIgnoreCase(moveMode)) {
                mod = mod + 1;
            }
        }
        return mod;
    }

    public static int weaponMod(String weapon, int range) {
        int mod = 0;
        if (weapon != null && !"".equalsIgnoreCase(weapon)) {
            if ("f".equalsIgnoreCase(weapon)) {
                switch (range) {
                    case 1:
                        mod = 0;
                        break;
                    case 2:
                        mod = 2;
                        break;
                    case 3:
                        mod = 4;
                        break;
                    default:
                        mod = 100;
                }
            } else if ("ll".equalsIgnoreCase(weapon)) {
                switch (range) {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        mod = 0;
                        break;
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                    case 10:
                        mod = 2;
                        break;
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                        mod = 4;
                        break;
                    default:
                        mod = 100;
                }
            } else if ("ml".equalsIgnoreCase(weapon)) {
                switch (range) {
                    case 1:
                    case 2:
                    case 3:
                        mod = 0;
                        break;
                    case 4:
                    case 5:
                    case 6:
                        mod = 2;
                        break;
                    case 7:
                    case 8:
                    case 9:
                        mod = 4;
                        break;
                    default:
                        mod = 100;
                }
            } else if ("sl".equalsIgnoreCase(weapon)) {
                switch (range) {
                    case 1:
                        mod = 0;
                        break;
                    case 2:
                        mod = 2;
                        break;
                    case 3:
                        mod = 4;
                        break;
                    default:
                        mod = 100;
                }
            } else if ("ppc".equalsIgnoreCase(weapon)) {
                switch (range) {
                    case 1:
                        mod = 3;
                        break;
                    case 2:
                        mod = 2;
                        break;
                    case 3:
                        mod = 1;
                        break;
                    case 4:
                    case 5:
                    case 6:
                        mod = 0;
                        break;
                    case 7:
                    case 8:
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                        mod = 2;
                        break;
                    case 13:
                    case 14:
                    case 15:
                    case 16:
                    case 17:
                    case 18:
                        mod = 4;
                        break;
                    default:
                        mod = 100;
                }
            } else if ("ac2".equalsIgnoreCase(weapon)) {
                switch (range) {
                    case 1:
                        mod = 4;
                        break;
                    case 2:
                        mod = 3;
                        break;
                    case 3:
                        mod = 2;
                        break;
                    case 4:
                        mod = 1;
                        break;
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                        mod = 0;
                        break;
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                    case 16:
                        mod = 2;
                        break;
                    case 17:
                    case 18:
                    case 19:
                    case 20:
                    case 21:
                    case 22:
                    case 23:
                    case 24:
                        mod = 4;
                        break;
                    default:
                        mod = 100;
                }
            } else if ("ac5".equalsIgnoreCase(weapon)) {
                switch (range) {
                    case 1:
                        mod = 3;
                        break;
                    case 2:
                        mod = 2;
                        break;
                    case 3:
                        mod = 1;
                        break;
                    case 4:
                    case 5:
                    case 6:
                        mod = 0;
                        break;
                    case 7:
                    case 8:
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                        mod = 2;
                        break;
                    case 13:
                    case 14:
                    case 15:
                    case 16:
                    case 17:
                    case 18:
                        mod = 4;
                        break;
                    default:
                        mod = 100;
                }
            } else if ("ac10".equalsIgnoreCase(weapon)) {
                switch (range) {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        mod = 0;
                        break;
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                    case 10:
                        mod = 2;
                        break;
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                        mod = 4;
                        break;
                    default:
                        mod = 100;
                }
            } else if ("ac20".equalsIgnoreCase(weapon)) {
                switch (range) {
                    case 1:
                    case 2:
                    case 3:
                        mod = 0;
                        break;
                    case 4:
                    case 5:
                    case 6:
                        mod = 2;
                        break;
                    case 7:
                    case 8:
                    case 9:
                        mod = 4;
                        break;
                    default:
                        mod = 100;
                }
            } else if ("mg".equalsIgnoreCase(weapon)) {
                switch (range) {
                    case 1:
                        mod = 0;
                        break;
                    case 2:
                        mod = 2;
                        break;
                    case 3:
                        mod = 4;
                        break;
                    default:
                        mod = 100;
                }
            } else if ("lrm5".equalsIgnoreCase(weapon) ||
                    "lrm10".equalsIgnoreCase(weapon) ||
                    "lrm15".equalsIgnoreCase(weapon) ||
                    "lrm20".equalsIgnoreCase(weapon)) {
                switch (range) {
                    case 1:
                        mod = 6;
                        break;
                    case 2:
                        mod = 5;
                        break;
                    case 3:
                        mod = 4;
                        break;
                    case 4:
                        mod = 3;
                        break;
                    case 5:
                        mod = 2;
                        break;
                    case 6:
                        mod = 1;
                        break;
                    case 7:
                        mod = 0;
                        break;
                    case 8:
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                        mod = 2;
                        break;
                    case 15:
                    case 16:
                    case 17:
                    case 18:
                    case 19:
                    case 20:
                    case 21:
                        mod = 4;
                        break;
                    default:
                        mod = 100;
                }
            } else if ("srm2".equalsIgnoreCase(weapon) ||
                    "srm4".equalsIgnoreCase(weapon) ||
                    "srm6".equalsIgnoreCase(weapon)) {
                switch (range) {
                    case 1:
                    case 2:
                    case 3:
                        mod = 0;
                        break;
                    case 4:
                    case 5:
                    case 6:
                        mod = 2;
                        break;
                    case 7:
                    case 8:
                    case 9:
                        mod = 4;
                        break;
                    default:
                        mod = 100;
                }
            }
        }
        return mod;
    }

    public static int physicalAttackerMod(String moveMode, String position) {
        int mod = 0;
        // Move Mode.
        if (moveMode != null && "walk".equalsIgnoreCase(moveMode)) {
            mod = mod + 1;
        } else if (moveMode != null && ("run".equalsIgnoreCase(moveMode) || "charge".equalsIgnoreCase(moveMode))) {
            mod = mod + 2;
        } else if (moveMode != null && ("jump".equalsIgnoreCase(moveMode) || "pounce".equalsIgnoreCase(moveMode))) {
            mod = mod + 3;
        }
        // Position.
        if (position != null && "prone".equalsIgnoreCase(position)) {
            mod = mod + 2;
        }
        return mod;
    }

    public static int physicalTargetMod(String moveMode, int moveDistance, String position) {
        int mod = 0;
        // Position
        if (position != null) {
            if ("prone".equalsIgnoreCase(position)) {
                mod = mod - 2;
            }
        }
        // Movement
        switch (moveDistance) {
            case 0:
            case 1:
            case 2:
                break;
            case 3:
            case 4:
                mod = mod + 1;
                break;
            case 5:
            case 6:
                mod = mod + 2;
                break;
            case 7:
            case 8:
            case 9:
                mod = mod + 3;
                break;
            default:
                mod = mod + 4;
        }
        if (moveMode != null) {
            if ("jump".equalsIgnoreCase(moveMode) || "pounce".equalsIgnoreCase(moveMode)) {
                mod = mod + 1;
            }
        }
        return mod;
    }

    public static int physicalAttackMod(String physAttack) {
        int mod = 0;
        if (physAttack != null) {
            if ("punchL".equalsIgnoreCase(physAttack) || "punchR".equalsIgnoreCase(physAttack)) {
                mod = 4;
            } else if ("kick".equalsIgnoreCase(physAttack)) {
                mod = 3;
            } else if ("push".equalsIgnoreCase(physAttack)) {
                mod = 4;
            } else if ("charge".equalsIgnoreCase(physAttack)) {
                mod = 5;
            } else if ("pounce".equalsIgnoreCase(physAttack)) {
                mod = 5;
            } else if ("club".equalsIgnoreCase(physAttack)) {
                mod = 4;
            }
        }
        return mod;
    }
}
